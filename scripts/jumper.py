import os
import time
import schedule
from datetime import datetime, timedelta

from kenpompy import FanMatch
from kenpompy.utils import login

# Replace with your KenPom credentials
USERNAME = "matthew.j.heider@gmail.com"
PASSWORD = "Monday#15"

# Directory for shared data
DATA_DIR = "/app/data"


def get_date_string(offset=0):
    """Helper function to get date string in 'YYYY-MM-DD' format"""
    date = datetime.now() + timedelta(days=offset)
    return date.strftime('%Y-%m-%d')


def save_to_file(csv_data, file_name):
    """Save CSV to local file system"""
    # Ensure directory exists
    os.makedirs(DATA_DIR, exist_ok=True)

    full_path = os.path.join(DATA_DIR, file_name)
    with open(full_path, 'w') as f:
        f.write(csv_data)
    print(f"Data saved to {full_path}")


def fetch_and_save_kenpom_data():
    try:
        print(f"[{datetime.now()}] Starting KenPom data fetch...")
        # Authenticate and create a session
        session = login(USERNAME, PASSWORD)
        print("Successfully logged in to KenPom")

        # Get current day and next day dates
        current_date = get_date_string(0)
        next_date = get_date_string(1)

        print(f"Fetching data for {current_date}...")
        # Fetch FanMatch data for the current day
        fanmatch_current = FanMatch.FanMatch(session, current_date)
        if fanmatch_current.fm_df is not None:
            df_current = fanmatch_current.fm_df
            csv_current = df_current.to_csv(index=False)

            # Save locally
            filename = f"kenpom_fanmatch_{current_date}.csv"
            save_to_file(csv_current, filename)
            print(f"Successfully saved {filename}")
        else:
            print(f"No data available for {current_date}")

        print(f"Fetching data for {next_date}...")
        # Fetch FanMatch data for the next day
        fanmatch_next = FanMatch.FanMatch(session, next_date)
        if fanmatch_next.fm_df is not None:
            df_next = fanmatch_next.fm_df
            csv_next = df_next.to_csv(index=False)

            # Save locally
            filename = f"kenpom_fanmatch_{next_date}.csv"
            save_to_file(csv_next, filename)
            print(f"Successfully saved {filename}")
        else:
            print(f"No data available for {next_date}")

        print(f"[{datetime.now()}] KenPom data fetch completed successfully")
        return True

    except Exception as e:
        print(f"[{datetime.now()}] Error fetching KenPom data: {e}")
        return False


if __name__ == "__main__":
    # Run immediately on startup
    fetch_and_save_kenpom_data()

    # Schedule to run at Midnight every day
    schedule.every().day.at("00:00").do(fetch_and_save_kenpom_data)

    print(f"[{datetime.now()}] Scheduler started. Will run daily at 00:00")

    # Keep the script running
    while True:
        schedule.run_pending()
        time.sleep(60)  # Check every minute