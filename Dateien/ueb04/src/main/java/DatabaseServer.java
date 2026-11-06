import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ueb04.database.AddRecordRequest;
import ueb04.database.AddRecordResponse;
import ueb04.database.DatabaseServiceGrpc;
import ueb04.database.GetRecordRequest;
import ueb04.database.GetRecordResponse;
import ueb04.database.GetSizeRequest;
import ueb04.database.GetSizeResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DatabaseServer {
    private static final int DEFAULT_PORT = 45124;

    private final Server server;

    private DatabaseServer(int port) {
        server = ServerBuilder.forPort(port)
            .addService(new DatabaseService())
            .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = args.length > 0 ? parsePort(args[0]) : DEFAULT_PORT;
        DatabaseServer databaseServer = new DatabaseServer(port);
        databaseServer.start();
        databaseServer.blockUntilShutdown();
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("port must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("port must be a number", e);
        }
    }

    private void start() throws IOException {
        server.start();
        System.out.printf("gRPC database server listening on port %d%n", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown()));
    }

    private void blockUntilShutdown() throws InterruptedException {
        server.awaitTermination();
    }

    private static final class DatabaseService extends DatabaseServiceGrpc.DatabaseServiceImplBase {
        private final Map<Integer, String> records = new ConcurrentHashMap<>();

        @Override
        public void getRecord(
                GetRecordRequest request, StreamObserver<GetRecordResponse> responseObserver) {
            String record = records.get(request.getIndex());
            if (record == null) {
                responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no record found for index " + request.getIndex())
                    .asRuntimeException());
                return;
            }
            responseObserver.onNext(GetRecordResponse.newBuilder()
                .setFound(true)
                .setRecord(record)
                .build());
            responseObserver.onCompleted();
        }

        @Override
        public void addRecord(
                AddRecordRequest request, StreamObserver<AddRecordResponse> responseObserver) {
            records.put(request.getIndex(), request.getRecord());
            System.out.println("Added record: " + request.getIndex() + " -> " + request.getRecord());
            responseObserver.onNext(AddRecordResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }

        @Override
        public void getSize(
                GetSizeRequest request, StreamObserver<GetSizeResponse> responseObserver) {
            responseObserver.onNext(GetSizeResponse.newBuilder()
                .setSize(records.size())
                .build());
            responseObserver.onCompleted();
        }
    }
}
