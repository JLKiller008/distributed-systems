import java.io.IOException;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class DatabaseServer extends DatabaseServiceGrpc.DatabaseServiceImplBase {

    Database db;

    public static void main(String[] args) throws InterruptedException, IOException {
        new DatabaseServer().startServer();
    }

    @Override
    public void getRecord(KeyValue requestKey, StreamObserver<GetResponse> responseObserver) {
        String value = db.getRecord(requestKey.getKey());
        boolean found = true;
        if (value == null) {
            found = false;
            value = "";
        }

        GetResponse reply = GetResponse.newBuilder().setFound(found).setValue(value).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void addRecord(PutParameters putParams, StreamObserver<Empty> responseObserver) {
        db.addRecord(putParams.getKey(), putParams.getValue());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getSize(Empty empty, StreamObserver<SizeValue> responseObserver) {
        int size = db.getSize();

        SizeValue reply = SizeValue.newBuilder().setSize(size).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    public void startServer() throws IOException, InterruptedException {
        db = new Database();

        Server server = ServerBuilder.forPort(50051)
                .addService(this)
                .build()
                .start();
        System.out.println("Database server running on port 50051");
        server.awaitTermination();
    }
}
