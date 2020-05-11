from pyp2p.net import *

bob = Net(passive_bind="deoni.cs.williams.edu", passive_port=44445, interface="eth0:1", node_type="passive", debug=1)
bob.start()
bob.bootstrap()
bob.advertise()

while 1:
    for con in bob:
        con.send_line("test")

    time.sleep(1)
