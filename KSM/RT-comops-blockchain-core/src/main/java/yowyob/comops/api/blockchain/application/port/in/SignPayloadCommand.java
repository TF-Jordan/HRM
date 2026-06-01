package yowyob.comops.api.blockchain.application.port.in;

public record SignPayloadCommand(String privateKey, String payload) {
}
