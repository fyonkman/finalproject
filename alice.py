from pyp2p.net import *
import time

alice = Net(node_type="passive", nat_type="preserving", passive_port=9173, net_type="direct")
alice.start()
print("started")
alice.bootstrap()
alice.advertise()

while 1:
    print("in while")
    for con in alice:
        print("hello")
        for reply in con:
            print(reply)

    time.sleep(1)

