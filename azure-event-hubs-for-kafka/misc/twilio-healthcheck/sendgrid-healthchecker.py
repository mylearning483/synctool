import os
import time
from configparser import ConfigParser
from datetime import datetime
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail

config = ConfigParser()
config.read('config.ini')

# SendGrid configuration
api_key = os.getenv('SENDGRID_API_KEY', config.get('sendgrid', 'api_key'))
from_email = config.get('sendgrid', 'from_email')
to_email = config.get('sendgrid', 'to_email')
message = config.get('sendgrid', 'message')

# Health check function
def sendgrid_health_check():
    try:
        # Send message
        sg = SendGridAPIClient(api_key)
        message = Mail(from_email=from_email, to_emails=to_email, subject='SendGrid Health Check', plain_text_content=message)
        response = sg.send(message)
        
        # Check delivery status
        if response.status_code != 202:
            raise Exception('Failed to send message: {}'.format(response.body))
        print('Message sent successfully')
    
    except Exception as e:
        print('Error sending message: {}'.format(e))
        # Call alert function here
        # alert()

# Run health check every n seconds
while True:
    print('\nSending message at {}'.format(datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
    sendgrid_health_check()
    time.sleep(60) # Change to desired interval in seconds
