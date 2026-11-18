"""gRPC-Datenbankserver (Aufgabe 4). Aufruf: python server.py [port]"""
import sys
import threading
from concurrent import futures

import grpc
import DatabaseService_pb2 as pb
import DatabaseService_pb2_grpc as rpc


class Database(rpc.DatabaseServicer):
    """Gleiche Logik wie Database.java aus ueb03: Dictionary + Lock."""

    def __init__(self):
        self.records = {}
        self.lock = threading.Lock()

    def GetRecord(self, request, context):
        with self.lock:
            record = self.records.get(request.index)
        if record is None:
            context.abort(grpc.StatusCode.NOT_FOUND, f"no record for index {request.index}")
        return pb.GetRecordReply(record=record)

    def AddRecord(self, request, context):
        with self.lock:
            self.records[request.index] = request.record
        return pb.AddRecordReply()

    def GetSize(self, request, context):
        with self.lock:
            return pb.GetSizeReply(size=len(self.records))


def main():
    port = sys.argv[1] if len(sys.argv) > 1 else "4448"
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))  # Thread-Pool statt "Thread pro Request"
    rpc.add_DatabaseServicer_to_server(Database(), server)
    server.add_insecure_port(f"[::]:{port}")
    server.start()
    print(f"gRPC database server listening on port {port}")
    server.wait_for_termination()


if __name__ == "__main__":
    main()
