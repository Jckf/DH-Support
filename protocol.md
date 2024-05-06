# Distant Horizons Protocol

Based on information from [here](https://gitlab.com/s809/minecraft-lod-mod/-/wikis/Protocol) and the Distant Horizons source code.

## Data types

### Strings
Strings are UTF-8 encoded, prefixed by an int containing their length.

### Short strings
Short strings are UTF-8 encoded, prefixed by a short containing their length.

### Optional fields
Optional fields are prefixed by a single byte with value 0 or 1 to signal their presence.

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

Additional header data for trackable messages.
* (int) tracker

### 0x0001 Hello

* (int) protocol version

### 0x0002 Close reason

* (short) reason length
* (string) reason

### 0x0003 Ack (trackable)

No additional data.

### 0x0004 Cancel (trackable)

No additional data.

### 0x0005 Exception (trackable)

* (int) exception type
* (short) message length
* (string) message

### 0x0006 Player UUID (trackable)

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

### 0x0008 Full data request (trackable)

* (short string) level name
* (byte) detail level
* (int) x coordinate
* (int) z coordinate
* (bool) checksum presence
   * (int) checksum

### 0x0009 Full data response (trackable)

This message contains compressed data. Each of the entries below suffixed with `(compressed)` start with a single uncompressed int denoting the length in bytes of the compressed data. Everything else is compressed according to the `compression type` field.

* (bool) is full
* position
   * (byte) detail level
   * (int) x coordinate
   * (int) z coordinate
* (int) checksum
* LOD (compressed)
   * (int) compressed data length
   * for each column
      * (short) number of data points
      * for each data point
         * (long) data point
* column generation steps (compressed)
   * (int) compressed data length
   * for each column
      * (byte) generation step (0: Empty. 1: Structure start. 2: Structure reference. 3: Biomes. 4: Noise. 5: Surface. 6: Carvers. 7: Liquid carvers. 8: Features. 9: Light.)
* column compression types (compressed)
   * (int) compressed data length
   * for each column
      * (byte) compression type (0: Strict. 1: Loose.)
* mappings (compressed)
   * (int) compressed data length
   * (int) number of mappings
   * for each mapping
      * (short string) concatenated string containing biome, block type, and block state
* (byte) data format version
* (byte) compression type (0: None. 1: LZ4. 2: ZSTD. 3: LZMA.)
* (bool) apply to parent
* (long) last modified timestamp
* (long) created timestamp

### 0x000a Partial update

* (short string) level name
* (int) x coordinate
* (int) z coordinate
* (int) data length
* data

### 0x000b Generation task priority request (trackable)

* (short string) level name
* (int) position list length
* position list

### 0x000c Generation task priority response (trackable)

* (int) position list length
* position list
