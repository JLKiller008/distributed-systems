"""gRPC-Datenbankclient (Aufgabe 4). Aufruf: python client.py [host] [port]"""
import sys

import grpc
import DatabaseService_pb2 as pb
import DatabaseService_pb2_grpc as rpc

DATA = {4101: "Appen", 4102: "Ahrensburg", 4103: "Wedel",
        4104: "Aumühle", 4105: "Seevetal", 4106: "Quickborn"}


def main():
    host = sys.argv[1] if len(sys.argv) > 1 else "localhost"
    port = sys.argv[2] if len(sys.argv) > 2 else "4448"

    with grpc.insecure_channel(f"{host}:{port}") as channel:
        stub = rpc.DatabaseStub(channel)  # Aufrufe sehen aus wie lokale Methodenaufrufe

        for index, record in DATA.items():
            stub.AddRecord(pb.AddRecordRequest(index=index, record=record))
            print(f"addRecord({index}, {record}): ok")

        for index in (4103, 4107):
            try:
                print(f"getRecord({index}): {stub.GetRecord(pb.GetRecordRequest(index=index)).record}")
            except grpc.RpcError as e:
                print(f"getRecord({index}): FEHLER {e.code().name} - {e.details()}")

        size = stub.GetSize(pb.GetSizeRequest()).size
        print(f"getSize(): {size} (erwartet: {len(DATA)})")


if __name__ == "__main__":
    main()
