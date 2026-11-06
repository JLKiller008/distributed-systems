import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ueb04.database.AddRecordRequest;
import ueb04.database.DatabaseServiceGrpc;
import ueb04.database.GetRecordRequest;
import ueb04.database.GetSizeRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class DatabaseClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 45124;

    public static void main(String[] args) throws InterruptedException {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? parsePort(args[1]) : DEFAULT_PORT;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
        try {
            run(channel);
        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
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

    private static void run(ManagedChannel channel) {
        DatabaseServiceGrpc.DatabaseServiceBlockingStub database =
            DatabaseServiceGrpc.newBlockingStub(channel);
        Map<Integer, String> records = new LinkedHashMap<>();
        records.put(4101, "Appen");
        records.put(4102, "Ahrensburg");
        records.put(4103, "Wedel");
        records.put(4104, "Aumühle");
        records.put(4105, "Seevetal");
        records.put(4106, "Quickborn");

        for (Map.Entry<Integer, String> entry : records.entrySet()) {
            database.addRecord(AddRecordRequest.newBuilder()
                .setIndex(entry.getKey())
                .setRecord(entry.getValue())
                .build());
        }

        printRecord(database, 4103);
        printRecord(database, 4107);
        int size = database.getSize(GetSizeRequest.getDefaultInstance()).getSize();
        System.out.println("getSize(): " + size);
    }

    private static void printRecord(
            DatabaseServiceGrpc.DatabaseServiceBlockingStub database, int index) {
        try {
            String record = database.getRecord(GetRecordRequest.newBuilder()
                .setIndex(index)
                .build()).getRecord();
            System.out.printf("getRecord(%d): %s%n", index, record);
        } catch (StatusRuntimeException e) {
            System.out.printf("getRecord(%d): %s%n", index, e.getStatus().getDescription());
        }
    }
}
