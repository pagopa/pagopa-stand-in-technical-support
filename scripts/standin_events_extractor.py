#!/usr/bin/env python3

"""
python standin_events_extractor.py \
--start-date 2025-03-01 \
--end-date 2025-03-31 \
--cosmos-connection-string "your-conn-str"
"""

import argparse
import csv
import datetime
import logging
import os
import sys
from datetime import timezone

from azure.cosmos import CosmosClient
from slack_sdk import WebClient
from slack_sdk.errors import SlackApiError

# Configurazione del logging
logging.basicConfig(
    level=logging.ERROR,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("standin-events-extractor")
logger.setLevel(logging.INFO)


def parse_arguments():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(description='Extract stand-in events from Cosmos DB')

    # Parametri per date e output
    parser.add_argument('--start-date', default='2025-04-30', help='Start date in YYYY-MM-DD format')
    parser.add_argument('--end-date', default=get_yesterday_date(), help='End date in YYYY-MM-DD format')
    parser.add_argument('--output', default='standin_events.csv', help='Output CSV file name')

    # Parametri per connessione a Cosmos DB
    parser.add_argument('--cosmos-connection-string', required=True,
                        help='Cosmos DB connection string')
    parser.add_argument('--cosmos-database', default='standin',
                        help='Cosmos DB database name')
    parser.add_argument('--cosmos-container', default='events',
                        help='Cosmos DB container name')
    parser.add_argument('--slack-webapi-token', required=True,
                        help='Token for Slack Web API bot')
    parser.add_argument('--slack-channel-id', required=True,
                        help='Token for Slack Web API bot')

    return parser.parse_args()


def get_cosmos_client(args):
    """Create and return a Cosmos DB client using connection string"""
    try:
        connection_string = args.cosmos_connection_string
        client = CosmosClient.from_connection_string(connection_string, preferred_locations=['North Europe'])
        return client
    except Exception as e:
        logger.error(f"Failed to create Cosmos DB client: {str(e)}")
        sys.exit(1)


def query_standin_events(client: CosmosClient, args, start_date, end_date):
    """Query Cosmos DB for stand-in events within the date range"""
    database_name = args.cosmos_database
    container_name = args.cosmos_container

    database = client.get_database_client(database_name)
    container = database.get_container_client(container_name)

    # Create a list of partition keys (dates) to query
    partition_keys = []
    current_date = datetime.datetime.strptime(start_date, "%Y-%m-%d")
    end_date_obj = datetime.datetime.strptime(end_date, "%Y-%m-%d")

    while current_date <= end_date_obj:
        partition_keys.append(current_date.strftime("%Y-%m-%d"))
        current_date += datetime.timedelta(days=1)

    # logger.info(f"Will query the following partition keys: {partition_keys}")

    all_events = []

    # Query for ADD_TO_STANDIN and REMOVE_FROM_STANDIN events
    for partition_key in partition_keys:
        query = """
        SELECT * FROM c 
        WHERE c.type IN ('ADD_TO_STANDIN', 'REMOVE_FROM_STANDIN')
        """

        try:
            logger.debug(f"Querying partition key: {partition_key}")
            items = list(container.query_items(
                query=query,
                partition_key=partition_key,
                enable_cross_partition_query=False
            ))
            logger.info(f"Retrieved {len(items)} events from partition {partition_key}")
            all_events.extend(items)
        except Exception as e:
            logger.warning(f"Error querying partition {partition_key}: {str(e).splitlines()[0]}")

    logger.info(f"Retrieved a total of {len(all_events)} events")
    return all_events


def convert_unix_timestamp(timestamp):
    """Convert Unix timestamp to ISO format"""
    if isinstance(timestamp, (int, float)):
        dt = datetime.datetime.fromtimestamp(timestamp, tz=timezone.utc)
        return dt.isoformat()
    return timestamp


def process_events(events):
    """Process events to pair ADD and REMOVE events for each station"""

    stations_data = {}

    # Sort events by station and timestamp
    events.sort(key=lambda x: (x.get("station", ""), x.get("timestamp", 0)))

    for event in events:
        event_type = event.get("type")
        station_id = event.get("station")
        timestamp = event.get("timestamp")

        if not station_id or timestamp is None:
            logger.warning(f"Skipping event with missing data: {event}")
            continue

        if station_id not in stations_data:
            stations_data[station_id] = []

        iso_timestamp = convert_unix_timestamp(timestamp)

        if event_type == "ADD_TO_STANDIN":
            stations_data[station_id].append({"start": iso_timestamp, "end": None})
        elif event_type == "REMOVE_FROM_STANDIN":
            # Find the most recent ADD event without a corresponding REMOVE
            for session in reversed(stations_data[station_id]):
                if session["end"] is None:
                    session["end"] = iso_timestamp
                    break

    # Calculate duration for each session and format the results
    result = []
    for station_id, sessions in stations_data.items():
        for session in sessions:
            start_time = session["start"]
            end_time = session["end"]

            if end_time is None:
                logger.warning(f"Station {station_id} has an open session that started at {start_time}")
                duration = "N/A"
            else:
                try:
                    # Convert ISO strings to datetime objects
                    start_dt = datetime.datetime.fromisoformat(start_time)
                    end_dt = datetime.datetime.fromisoformat(end_time)
                    
                    delta = end_dt - start_dt
                    days = delta.days
                    seconds = delta.seconds
                    hours = seconds // 3600
                    minutes = (seconds % 3600) // 60
                    seconds = seconds % 60
                    duration = f"{days}d {hours}h {minutes}m {seconds}s"
                except ValueError:
                    logger.warning(f"Error calculating duration for session {start_time} - {end_time}")
                    duration = "Error"

            result.append({
                "station_id": station_id,
                "start_time": start_time,
                "end_time": end_time if end_time else "N/A",
                "duration": duration
            })

    return result


def write_to_csv(data, header, output_file):
    """Write processed data to CSV file"""
    try:
        with open(output_file, 'w', newline='') as file:
            writer = csv.writer(file)
            writer.writerow([header])
            writer.writerow(["Station", "Start date", "End date", "Duration"])

            for item in data:
                writer.writerow([
                    item["station_id"],
                    item["start_time"],
                    item["end_time"],
                    item["duration"]
                ])

        logger.info(f"Results written to {output_file}")
    except Exception as e:
        logger.error(f"Error writing to CSV file: {str(e)}")
        sys.exit(1)


def get_yesterday_date():
    today = datetime.datetime.today()
    yesterday = today - datetime.timedelta(days=1)
    return yesterday.strftime('%Y-%m-%d')

def get_today_date():
    today = datetime.datetime.today()
    return today.strftime('%Y-%m-%d')


def main():
    """Main function"""
    args = parse_arguments()

    today_date = get_today_date()
    start_date = args.start_date
    end_date = args.end_date
    header = f"Report extracted from events in date range: [{start_date} - {end_date}]"
    output_file = args.output + "_" + today_date

    logger.info(f"Extracting stand-in events from {start_date} to {end_date}")

    client = get_cosmos_client(args)
    events = query_standin_events(client, args, start_date, end_date)
    processed_data = process_events(events)
    write_to_csv(processed_data, header, output_file)

    bot_token = args.slack_webapi_token
    channel_id = args.slack_channel_id
    
    client = WebClient(token = bot_token)
    try:
        result = client.files_upload_v2(
            channel = channel_id,
            initial_comment = f"Generazione del report cumulativo per le stazioni in Stand-In [{today_date}]",
            file = output_file,
        )
        logger.info(f"Response from Slack. Is OK? [{result['ok']}]")

    except SlackApiError as e:
        logger.error("Error uploading file: {}".format(e))
  
    logger.info("Operation completed successfully")


if __name__ == "__main__":
    main()
