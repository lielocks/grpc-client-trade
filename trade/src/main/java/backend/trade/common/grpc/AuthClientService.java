package backend.trade.common.grpc;

import auth.Auth;
import auth.AuthServiceGrpc;
import backend.trade.common.constant.Constant;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthClientService {

    private final ManagedChannel channel;
    @GrpcClient("security-grpc-server")
    private final AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    public AuthClientService(@Value("${grpc.auth.host}") String host,
                             @Value("${grpc.auth.port}") int port) {
        System.out.println("host : " + host);
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.authServiceStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    public boolean verifyToken(String token) {
        try {
            // header 에 token 정보 추가
            Metadata headers = createHeaders(token);

            // interceptor 로 header 정보 추가
            ClientInterceptor authInterceptor = MetadataUtils.newAttachHeadersInterceptor(headers);

            // authServiceStub 에 interceptor 붙이기
            AuthServiceGrpc.AuthServiceBlockingStub stubWithCallCredentials =
                    authServiceStub.withInterceptors(authInterceptor);

            // TokenRequest 보내기
            Auth.TokenRequest request = Auth.TokenRequest.newBuilder()
                    .setToken(token)
                    .build();

            Auth.TokenResponse response = stubWithCallCredentials.verifyToken(request);
            return response.getIsValid();
        } catch (Exception e) {
            log.error("Error verifying token :: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Auth.TokenRequest request = Auth.TokenRequest.newBuilder()
                .setToken(token)
                .build();

        Auth.TokenResponse response = authServiceStub.verifyToken(request);

        if (response.getIsValid()) {
            return response.getUserId();
        } else {
            throw new IllegalArgumentException(Constant.INVALID_ACCESS_TOKEN);
        }
    }

    private Metadata createHeaders(String token) {
        Metadata headers = new Metadata();
        Metadata.Key<String> authHeaderKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authHeaderKey, "Bearer " + token);
        return headers;
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
