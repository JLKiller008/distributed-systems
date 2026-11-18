import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DatabaseClient {

    public static void main(String[] args) {
        new DatabaseClient().runClient();
    }

    public void runClient() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        DatabaseServiceGrpc.DatabaseServiceBlockingStub stub = DatabaseServiceGrpc.newBlockingStub(channel);

        PutParameters req = PutParameters.newBuilder().setKey(4101).setValue("Appen").build();
        stub.addRecord(req);
        req = PutParameters.newBuilder().setKey(4102).setValue("Ahrensburg").build();
        stub.addRecord(req);
        req = PutParameters.newBuilder().setKey(4103).setValue("Wedel").build();
        stub.addRecord(req);
        req = PutParameters.newBuilder().setKey(4104).setValue("Aumühle").build();
        stub.addRecord(req);
        req = PutParameters.newBuilder().setKey(4105).setValue("Seevetal").build();
        stub.addRecord(req);
        req = PutParameters.newBuilder().setKey(4106).setValue("Quickborn").build();
        stub.addRecord(req);

        System.out.println("Checking entry 4103");
        KeyValue getReq = KeyValue.newBuilder().setKey(4103).build();
        GetResponse resp = stub.getRecord(getReq);
        if (resp.getFound()) {
            System.out.println("Entry found: " + resp.getValue());
        } else {
            System.out.println("Entry not found");
        }

        System.out.println("Checking entry 4107");
        getReq = KeyValue.newBuilder().setKey(4107).build();
        resp = stub.getRecord(getReq);
        if (resp.getFound()) {
            System.out.println("Entry found: " + resp.getValue());
        } else {
            System.out.println("Entry not found");
        }

        SizeValue sizeResp = stub.getSize(Empty.getDefaultInstance());
        System.out.println("Database size: " + sizeResp.getSize());

        channel.shutdown();
    }

}
