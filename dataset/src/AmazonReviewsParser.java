import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.UUID;

public class AmazonReviewsParser {
    public static void main(String[] args) {
        RunParams params = RunParams.parse(args);
        System.out.println("Run with " + params);
        if (params.sourceFile == null || params.sourceFile.isEmpty()
                || params.targetFile == null || params.targetFile.isEmpty()) {
            System.err.println("Usage: AmazonReviewsParser --sourceFile <source data file> --targetFile <target data file> [--maxCount <max reviews count>]");
        }

        new AmazonReviewsParser().process(params);
    }

    private void process(RunParams params) {
        System.out.println("Parse data file: " + params.sourceFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(params.sourceFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(params.targetFile))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String review = extractReviewText(line);
                if (review != null && !review.isEmpty()) {
                    writer.write(review);
                    writer.write('\n');
                    count++;
                }
                if (params.maxCount > 0 && count >= params.maxCount) {
                    break;
                }
            }

            System.out.println("Succeeded to parse reviews, count: " + count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractReviewText(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            String review = json.optString("reviewText");
            String reviewerName = json.optString("reviewerName");
            if (!review.isEmpty()) {
                JSONObject newJson = new JSONObject();
                newJson.put("dataId", UUID.randomUUID());
                newJson.put("desc", review);
                newJson.put("author", reviewerName);
                return newJson.toString();
            }
        } catch (JSONException e) {
            // ignore
        }
        return null;
    }

    static class RunParams {
        String sourceFile;
        String targetFile;
        int maxCount;

        static RunParams parse(String[] args) {
            RunParams params = new RunParams();
            for (String p : args) {
                String[] fields = p.split("=");
                switch (fields[0]) {
                    case "--sourceFile": {
                        params.sourceFile = fields[1];
                        break;
                    }
                    case "--targetFile": {
                        params.targetFile = fields[1];
                        break;
                    }
                    case "--maxCount": {
                        params.maxCount = Integer.parseInt(fields[1]);
                        break;
                    }
                    default: {
                        System.out.println("Unknown param: " + p);
                        break;
                    }
                }
            }
            return params;
        }

        @Override
        public String toString() {
            return "RunParams[sourceFile=" + sourceFile + ", targetFile=" + targetFile
                    + ", maxCount=" + maxCount + "]";
        }
    }
}
