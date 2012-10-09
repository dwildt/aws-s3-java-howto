package cc.ttlabs.aws.s3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class AWSFileMapperBucketTest {
	
	private FileMapper om = null;
	private static String accessKey = "";
	private static String secretKey = "";
	
	@BeforeClass
	public static void setUpClass()
	{
		try {
			Properties auth = AWSFileMapperHelper.readAuthProperties();
			if(auth != null){
				accessKey = auth.getProperty("accessKey");
				secretKey = auth.getProperty("secretKey");				
			}
		} catch (IOException e) {
			accessKey = "";
			secretKey = ""; 
		}
	}
	
	@Before
	public void setUp()
	{
		om = new AWSFileMapper(accessKey, secretKey, AWSFileMapper.AWS_REGION_SAOPAULO);
	}
	
	@After
	public void tearDown()
	{
		om = null;
	}
	
	@Test
	public void shouldBeAbleToCreateAndRemoveAnEmptyBucket()
	{
		String rootFolderName = "my-bucket-" + UUID.randomUUID();
		
		System.out.println(rootFolderName);
		assertFalse(om.isRootFolder(rootFolderName));
		
		if(!om.isRootFolder(rootFolderName))
		  om.createRootFolder(rootFolderName);
		
		assertTrue(om.isRootFolder(rootFolderName));
		
		om.deleteRootFolder(rootFolderName);
		assertFalse(om.isRootFolder(rootFolderName));
	}
}
