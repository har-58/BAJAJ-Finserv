@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String INIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";
    private static final String NAME = "John Doe";
    private static final String REG_NO = "REG12347";
    private static final String EMAIL = "john@example.com";

    public void processWebhook() {
        try {
            // 1. Generate webhook
            Map<String, String> request = Map.of("name", NAME, "regNo", REG_NO, "email", EMAIL);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(INIT_URL, entity, WebhookResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                WebhookResponse webhookResponse = response.getBody();
                int lastDigit = Integer.parseInt(REG_NO.replaceAll("\\D+", "")) % 10;

                List<Integer> result;
                if (lastDigit % 2 == 0) {
                    result = solveNthLevel(webhookResponse.getData());
                } else {
                    result = List.of(); // Mutual followers logic placeholder
                }

                sendToWebhook(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<Integer> solveNthLevel(WebhookResponse.Data data) {
        int findId = data.getFindId();
        int n = data.getN();
        Map<Integer, List<Integer>> graph = new HashMap<>();

        for (User u : data.getUsers()) {
            graph.put(u.getId(), u.getFollows());
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(findId);
        visited.add(findId);
        int level = 0;

        while (!queue.isEmpty() && level < n) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int curr = queue.poll();
                List<Integer> neighbors = graph.getOrDefault(curr, new ArrayList<>());
                for (int neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
            level++;
        }

        List<Integer> result = new ArrayList<>(queue);
        Collections.sort(result);
        return result;
    }

    private void sendToWebhook(String webhookUrl, String token, List<Integer> outcome) {
        Map<String, Object> payload = Map.of("regNo", REG_NO, "outcome", outcome);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        int attempts = 0;
        while (attempts < 4) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Successfully sent result to webhook.");
                    return;
                }
            } catch (Exception ex) {
                System.out.println("Attempt " + (attempts + 1) + " failed.");
            }
            attempts++;
        }
        System.out.println("Failed to send result to webhook after 4 attempts.");
    }
}
