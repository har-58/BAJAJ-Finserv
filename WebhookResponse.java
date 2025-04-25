@Data
public class WebhookResponse {
    private String webhook;
    private String accessToken;
    private Data data;

    @lombok.Data
    public static class Data {
        private int n;
        private int findId;
        private List<User> users;
    }
}
