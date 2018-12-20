import stomp
import time

class MyListener(object):
    def on_error(self, headers, message):
        print('received an error %s' % message)

    def on_message(self, headers, message):
        print('received a message %s' % message)

conn = stomp.Connection([('172.18.252.25', 61613), ('172.18.252.25', 62613)])
conn.set_listener('', MyListener())
conn.start()
print("connection started")
conn.connect(username="fsi", password="fsi")

time.sleep(5)

conn.subscribe(destination="/topic/test.smartvalve", ack="auto", id="testsubscriber1")

while True:
    pass

conn.disconnect()

