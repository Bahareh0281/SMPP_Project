import logging
import smpplib.client
import smpplib.gsm
import smpplib.consts
import time
from Crypto.Hash import SHA256

logging.basicConfig(level=logging.INFO)

class SMPPMessageHandler:
    def __init__(self):
        self.received_messages = set()
    
    def sha256_hash(self, message):
        hasher = SHA256.new()
        hasher.update(message.encode('utf-8'))
        return hasher.hexdigest()

    def send_sms(self, client, src_addr, dst_addr, message):
        parts, encoding_flag, msg_type_flag = smpplib.gsm.make_parts(message)

        for part in parts:
            pdu = client.send_message(
                source_addr_ton=smpplib.consts.SMPP_TON_INTL,
                source_addr_npi=smpplib.consts.SMPP_NPI_ISDN,
                source_addr=src_addr,
                dest_addr_ton=smpplib.consts.SMPP_TON_INTL,
                dest_addr_npi=smpplib.consts.SMPP_NPI_ISDN,
                destination_addr=dst_addr,
                short_message=part,
                data_coding=encoding_flag,
                esm_class=msg_type_flag,
                registered_delivery=True,
            )
            logging.info(f"SMS sent with PDU: {pdu}")

    # def decode_message(self, message_bytes):
    #     """Try to decode the message using different encodings."""
    #     encodings = ["utf-16", "utf-8", "gsm0338"]
    #     for encoding in encodings:
    #         try:
    #             return message_bytes.decode(encoding)
    #         except UnicodeDecodeError:
    #             continue
    #     # If all decodings fail, return a placeholder message
    #     return "[Undecodable message]"

    def handle_incoming_sms(self, pdu, client):
        try:
            logging.info(f"Incoming SMS PDU: {pdu}")
            source_address = pdu.source_addr.decode()
            logging.info(f"Source address: {source_address}")
            if str(source_address) != "11111":
                destination_address = pdu.destination_addr.decode()
                logging.info(f"Destination address: {destination_address}")

                logging.info(f"Message content: {pdu.short_message}")

                message_hash = self.sha256_hash(pdu.short_message)
                if message_hash not in self.received_messages:
                    self.received_messages.add(message_hash)
                    response_message = "پیام شما دریافت شد"
                    self.send_sms(client, destination_address, source_address, response_message)
                    logging.info("Acknowledgment sent.")
                    
        except Exception as e:
            logging.error(f"Error while handling incoming SMS: {e}")


    def receive_sms(self, client):
        while True:
            try:
                client.set_message_received_handler(lambda pdu: self.handle_incoming_sms(pdu, client))
                logging.info("Waiting for incoming SMS...")
                client.listen()
                time.sleep(1)  
            except KeyboardInterrupt:
                logging.info("Stopping listener...")
                break
            except Exception as e:
                logging.error(f"Error occurred: {e}")
                logging.info("Reconnecting...")
                time.sleep(5)
                try:
                    client.connect()
                    client.bind_transceiver(system_id=self.username, password=self.password)
                except Exception as e:
                    logging.error(f"Reconnection failed: {e}")
                    time.sleep(15)

    def send_and_receive_sms(self, host, port, username, password, src_addr):
        self.username = username
        self.password = password
        while True:
            try:
                logging.info(f"Connecting to SMPP server at {host}:{port}...")
                client = smpplib.client.Client(host, port)
                client.connect()
                client.bind_transceiver(system_id=username, password=password)
                logging.info("Connected and bound to SMPP server.")
                self.receive_sms(client)
                client.unbind()
                client.disconnect()
            except smpplib.exceptions.ConnectionError as e:
                logging.error(f"Connection error: {e}")
                logging.info("Retrying in 15 seconds...")
                time.sleep(15)
            except Exception as e:
                logging.error(f"Unexpected error: {e}")
                logging.info("Retrying in 15 seconds...")
                time.sleep(15)
                    
if __name__ == "__main__":
    host = '172.20.10.13'
    port = 9500
    username= "smppuser"
    password = "Ouo5nQM8"
    src_addr = '9126211842'
    
    smpp_handler = SMPPMessageHandler()
    smpp_handler.send_and_receive_sms(host, port, username, password, src_addr)