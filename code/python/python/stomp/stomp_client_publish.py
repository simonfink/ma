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
conn.connect(username="fsi", password="fsi")

time.sleep(5)

conn.send(body='hello from test publisher1', destination="/topic/test.smartvalve", id="testpublisher1")

time.sleep(5)

conn.disconnect()

