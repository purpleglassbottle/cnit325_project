/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cardgame;

/**
 *
 * @author emilyzhang
 */

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
//// Cassie
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SaveGame {
    private final AmazonS3 s3;
    private final String bucketName;

    public SaveGame(String bucketName, String region) {
        this.bucketName = bucketName;
        
//        AWSCredentialsProvider credentialsProvider = new EnvironmentVariableCredentialsProvider();
        this.s3 = AmazonS3ClientBuilder
                .standard()
//                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();
//        this.s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(
//        new BasicAWSCredentials("YOUR_ACCESS_KEY", "YOUR_SECRET_KEY"))).withRegion(region).build();
    }

    public void save(String gameId, String jsonState) {
        String key = "games/" + gameId + ".json";
        s3.putObject(bucketName, key, jsonState);
    }

    public Optional<String> load(String gameId) {
        String key = "games/" + gameId + ".json";
        if (!s3.doesObjectExist(bucketName, key)) {
            return Optional.empty();
        }

        S3Object obj = s3.getObject(bucketName, key);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(obj.getObjectContent(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return Optional.of(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from S3", e);
        }
    }
}
