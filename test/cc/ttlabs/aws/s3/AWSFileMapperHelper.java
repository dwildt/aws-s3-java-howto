package cc.ttlabs.aws.s3;

import java.io.IOException;
import java.util.Properties;

public class AWSFileMapperHelper {

	public static final String AWS_CREDENTIALS_FILE = "/cc/ttlabs/aws/s3/AwsCredentials.properties";

	public static Properties readAuthProperties() throws IOException
	{
		Properties auth = new Properties();
		try {
			auth.load(AWSFileMapperBucketTest.class.getResourceAsStream(AWS_CREDENTIALS_FILE));
			return auth;
			
		} catch (IOException e) {
			throw e; 
		}

	}
	
}
