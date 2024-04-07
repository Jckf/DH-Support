# Distant Horizons Protocol

## Plugin messages

Common header sent with all messages.
* (byte) null
* (short) protocol version
* (short) packet type

### 0x0001 Hello

No additional data.

### 0x0002 Level key

* (string) level key
* (bool) delete existing data

### 0x0003 Connect info

* (bool) address presence
   * (short) address length
   * (string) address
* (short) port

## Socket messages

Common header sent with all messages.
* (int) length
* (short) packet type

### 0x0001 Hello

* (int) protocol version

### 0x0002 Close reason

* (short) reason length
* (string) reason

### 0x0003 Ack

No additional data.

### 0x0004 Cancel

No additional data.

### 0x0005 Exception

* (int) exception type
* (short) message length
* (string) message

### 0x0006 Player UUID

* (long) UUID most significant bits
* (long) UUID least significant bits

### 0x0007 Player config

* (int) render distance
* (bool) distant generation enabled
* (int) full data request concurrency limit
* (int) generation task priority request rate limit
* (bool) real time updates enabled
* (bool) login data sync enabled
* (int) login data sync rate/concurrency limit
* (bool) generate multiple dimensions

### 0x0008 Full data request

* (int) level hash code (hash codes of detail level, x, and z XOR-ed together)
* (byte) detail level
* (int) x coordinate
* (int) z coordinate
* (bool) checksum presence
   * (int) checksum

### 0x0009 Full data response

* (bool) is full
* (byte) format version
* (int) data length
* data

### 0x000a Partial update

* (int) level hash code
* (int) x coordinate
* (int) z coordinate
* (int) data length
* data

### 0x000b Generation task priority request

* (int) level has code
* (int) position list length
* position list

### 0x000c Generation task priority response

* (int) position list length
* position list
