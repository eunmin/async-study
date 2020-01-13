"""
Opens a tcp socket to listen for client message(s) and
echoes the message(s) back to the client
"""

import sys
import time
from socket import *

def server(host, port):
    try:
        sock = socket(AF_INET, SOCK_STREAM) # creates a tcp socket object
        sock.bind((host, port))             # bind server to port
    except error:                           # print errors, if any...
        print('Socket creation failed!')
        print('Reason: %s'%str(error))
        sys.exit()                          # ...and exit
    sock.listen(5)                          # allow upto 5 queued connects
    print('Waiting for connection...')
    while True:                             # listen until killed
        conn, addr = sock.accept()	    # wait for next client
        print('New connection, %s:%s'%addr) # connection returns new socket
        while True:                         # until eof
            msg = conn.recv(1024)   	    # read next client message
            if not msg: break               # upon eof; client socket closed 
            print('Received:',msg.decode()) # print received message
            reply = msg #b'Echo: '+msg
            time.sleep(25) 
            conn.send(reply)                # echo reply
        conn.close()                        # close client connection
    sock.close()                            # never reached

if __name__=='__main__':
    host = ''
    port = 50007
    server(host, port)
