import configparser
import os
import time

import requests


def send_sms(api_key, api_secret, from_number, to_number, message):
    """Sends an SMS message using the Syniverse SMS API."""
    url = 'https://api.syniverse.com/scg-external-api/api/v1/sms/messages'
    headers = {
        'Authorization': f'Bearer {api_key}:{api_secret}',
        'Content-Type': 'application/json',
    }
    data = {
        'from': from_number,
        'to': to_number,
        'body': message,
    }
    response = requests.post(url, headers=headers, json=data)
    response.raise_for_status()
    return response.json()


def check_delivery_status(api_key, api_secret, message_id):
    """Checks the delivery status of an SMS message using the Syniverse SMS API."""
    url = f'https://api.syniverse.com/scg-external-api/api/v1/sms/messages/{message_id}/status'
    headers = {
        'Authorization': f'Bearer {api_key}:{api_secret}',
    }
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()['delivery_status']


def alert():
    """Sends an alert message if there was an error."""
    # Implement your alert logic here, e.g. send an email or a push notification.


if __name__ == '__main__':
    # Load configuration from config file with overrides from environment variables
    config = configparser.ConfigParser()
    config.read('config.ini')

    api_key = os.environ.get('SYNIVERSE_API_KEY') or config['syniverse']['api_key']
    api_secret = os.environ.get('SYNIVERSE_API_SECRET') or config['syniverse']['api_secret']
    from_number = os.environ.get('SYNIVERSE_FROM_NUMBER') or config['syniverse']['from_number']
    to_number = os.environ.get('SYNIVERSE_TO_NUMBER') or config['syniverse']['to_number']
    message = os.environ.get('SYNIVERSE_MESSAGE') or config['syniverse']['message']
    interval = int(os.environ.get('SYNIVERSE_INTERVAL') or config['syniverse']['interval'])

    # Main loop
    while True:
        try:
            # Send an SMS message
            response = send_sms(api_key, api_secret, from_number, to_number, message)
            message_id = response['id']

            # Check the delivery status of the SMS message
            delivery_status = check_delivery_status(api_key, api_secret, message_id)

            # If the delivery status indicates an error, call the alert() function
            if delivery_status != 'Delivered':
                alert()

        except requests.exceptions.RequestException as e:
            # If there was a network error, call the alert() function
            alert()

        time.sleep(interval)
