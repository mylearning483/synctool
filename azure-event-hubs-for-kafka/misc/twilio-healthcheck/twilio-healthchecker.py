import configparser
import os
import time
from twilio.rest import Client


config = configparser.ConfigParser()
config.read('config.ini')

account_sid = os.environ.get('TWILIO_ACCOUNT_SID', config['Twilio']['account_sid'])
auth_token = os.environ.get('TWILIO_AUTH_TOKEN', config['Twilio']['auth_token'])
from_number = os.environ.get('TWILIO_FROM_NUMBER', config['Twilio']['from_number'])
to_number = os.environ.get('TWILIO_TO_NUMBER', config['Twilio']['to_number'])
check_interval = int(os.environ.get('CHECK_INTERVAL', config['Twilio']['check_interval']))

client = Client(account_sid, auth_token)

def send_sms():
    message = client.messages.create(
        body='Twilio health check message',
        from_=from_number,
        to=to_number
    )
    return message.sid

def check_delivery_status(message_sid):
    message = client.messages(message_sid).fetch()
    return message.status

def alert():
    print('Twilio delivery status error. Alerting administrator...')


while True:
    message_sid = send_sms()
    time.sleep(10)  # Wait for message to be delivered
    status = check_delivery_status(message_sid)
    if status != 'delivered':
        alert()
    time.sleep(check_interval)
