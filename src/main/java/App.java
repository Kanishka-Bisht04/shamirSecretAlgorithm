import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import java.math.BigInteger;

public class App {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new FileInputStream("testcases.json");
        JsonNode testcases = mapper.readTree(inputStream);

        for (JsonNode testcase : testcases) {
            solveTestcase(testcase);
        }
    }

    static void solveTestcase(JsonNode root) {
        JsonNode keysNode = root.get("keys");
        int k = keysNode.get("k").asInt();
        int n = keysNode.get("n").asInt();

        List<Share> shareList = new ArrayList<>();
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (key.equals("keys")) continue;
            JsonNode share = root.get(key);
            int x = Integer.parseInt(key);
            int base = Integer.parseInt(share.get("base").asText());
            String value = share.get("value").asText();
            BigInteger y = new BigInteger(value, base);
            shareList.add(new Share(x, y));
        }

        // Use k shares for interpolation
        List<List<Share>> combinations = combinations(shareList, k);
        Map<BigInteger, Integer> secretCount = new HashMap<>();
        for (List<Share> combo : combinations) {
            BigInteger secret = lagrangeInterpolation(combo);
            secretCount.put(secret, secretCount.getOrDefault(secret, 0) + 1);
        }
        // Most common secret is the answer
        BigInteger correctSecret = secretCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .get().getKey();

        System.out.println(correctSecret);
    }

    static List<List<Share>> combinations(List<Share> arr, int k) {
        List<List<Share>> result = new ArrayList<>();
        combineHelper(arr, k, 0, new ArrayList<>(), result);
        return result;
    }

    static void combineHelper(List<Share> arr, int k, int start, List<Share> temp, List<List<Share>> result) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < arr.size(); i++) {
            temp.add(arr.get(i));
            combineHelper(arr, k, i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    // Lagrange interpolation at x=0 using BigInteger
    static BigInteger lagrangeInterpolation(List<Share> shares) {
        BigInteger secret = BigInteger.ZERO;
        int k = shares.size();
        for (int i = 0; i < k; i++) {
            BigInteger xi = BigInteger.valueOf(shares.get(i).x);
            BigInteger yi = shares.get(i).y;
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = BigInteger.valueOf(shares.get(j).x);
                    num = num.multiply(xj.negate());
                    den = den.multiply(xi.subtract(xj));
                }
            }
            BigInteger li = num.divide(den);
            secret = secret.add(yi.multiply(li));
        }
        return secret;
    }

    static class Share {
        int x;
        BigInteger y;
        Share(int x, BigInteger y) { this.x = x; this.y = y; }
    }
}
