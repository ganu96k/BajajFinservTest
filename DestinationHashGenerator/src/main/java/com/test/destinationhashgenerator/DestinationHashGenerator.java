package com.test.destinationhashgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <JSON file path>");
            return;
        }

        String prnNumber = args[0].toLowerCase().replaceAll("\\s+", "");
        String jsonFilePath = args[1];
        String destinationValue = "";

        try {
            destinationValue = findDestinationValue(jsonFilePath);
            if (destinationValue == null) {
                System.out.println("No 'destination' key found in the JSON file.");
                return;
            }

            String randomString = generateRandomString(8);
            String concatenatedString = prnNumber + destinationValue + randomString;
            String md5Hash = DigestUtils.md5Hex(concatenatedString);

            System.out.println(md5Hash + ";" + randomString);

        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
        }
    }

    private static String findDestinationValue(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

        return traverseJson(rootNode);
    }

    private static String traverseJson(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if ("destination".equals(entry.getKey())) {
                    return entry.getValue().asText();
                }
                String found = traverseJson(entry.getValue());
                if (found != null) {
                    return found;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                String found = traverseJson(arrayElement);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
