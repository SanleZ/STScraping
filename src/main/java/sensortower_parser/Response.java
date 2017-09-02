package sensortower_parser;

/**
 *
 */
public class Response {
    private final int code;
    private final byte[] body;

    public Response(int code, byte[] body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public byte[] getBody() {
        return body;
    }
}
