package cc.ttlabs.aws.s3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class AWSFileMapperTest {
	
	private static final String FULL_S3_END_POINT = "http://s3-sa-east-1.amazonaws.com/";

	public static final String AWS_UPLOAD_FILE = "/cc/ttlabs/aws/s3/trevisantecnologia_altaresolucao_aws.png";
	
	private FileMapper om = null;
	private String rootFolderName = "junit-bucket-" + UUID.randomUUID();

	private String folderName = "12345/";
	
	private static String accessKey = "";
	private static String secretKey = "";

	private String getNewFileName() {
		return UUID.randomUUID() + "trevisantecnologia_altaresolucao_aws.png";
	}

	
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
		
		assertFalse(om.isRootFolder(rootFolderName));
		
		if(!om.isRootFolder(rootFolderName))
		  om.createRootFolder(rootFolderName);
		assertTrue("Bucket not available: " + rootFolderName, om.isRootFolder(rootFolderName));		
	}
	
	@After
	public void tearDown()
	{
		om.deleteRootFolder(rootFolderName);
		assertFalse("Failed to remove Bucket " + rootFolderName, om.isRootFolder(rootFolderName));		

		om = null;
	}
	
	@Test
	public void shouldReturnFalseWhenObjectDoesNotExistInS3(){
		String keyNameNotAvailable = folderName + getNewFileName();
		File outputFile = null;
		try {
			outputFile = File.createTempFile("not-downloaded-file-aws-", ".png");
			assertFalse(om.downloadFile(rootFolderName, keyNameNotAvailable, outputFile));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	@Test
	public void shouldBeAbleToUploadFileToBucketAndDownloadToFile(){
		URL imageFile = getClass().getResource(AWS_UPLOAD_FILE);
		String keyName = folderName + getNewFileName();
		File outputFile = null;

		try {
			om.uploadFile(rootFolderName, keyName, new File(imageFile.toURI()));
			outputFile = File.createTempFile("downloaded-file-aws-", ".png");
			assertTrue(om.downloadFile(rootFolderName, keyName, outputFile));
			assertTrue("Error downloading file", outputFile.length() > 0);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			om.deleteFile(rootFolderName, keyName);
			om.deleteFile(rootFolderName, folderName);
		}
	}
	
	@Test
	public void shouldBeAbleToUploadFileAndMakeItPublic(){
		URL imageFile = getClass().getResource(AWS_UPLOAD_FILE);
		String keyName = folderName + getNewFileName();
		String publicURL = FULL_S3_END_POINT + rootFolderName + "/" + keyName;

		try {
			om.uploadFile(rootFolderName, keyName, new File(imageFile.toURI()));
			assertFalse(om.isFilePublic(rootFolderName, keyName));
			assertNull(om.getFileUrl(rootFolderName,keyName));
			om.makePublic(rootFolderName, keyName);
			assertTrue(om.isFilePublic(rootFolderName, keyName));
			assertEquals(publicURL, om.getFileUrl(rootFolderName,keyName));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			om.deleteFile(rootFolderName, keyName);
			om.deleteFile(rootFolderName, folderName);
		}
	}
	
	
	@Test
	public void shouldBeAbleToUploadAndDownloadUsingInputStreams(){
		InputStream outStream = null;
		InputStream inStream = null;
		File outputFile = null;
		FileOutputStream fo = null;

		URL imageFile = getClass().getResource(AWS_UPLOAD_FILE);
		String keyName = folderName + getNewFileName();
		try {
			inStream = new FileInputStream(new File(imageFile.toURI()));
			
			om.uploadFile(rootFolderName, keyName, inStream);
		
			outputFile = File.createTempFile("downloaded-11-file-aws-", ".png");
			outStream = om.downloadFile(rootFolderName, keyName);
			fo = new FileOutputStream(outputFile);
			byte[] bytes = IOUtils.toByteArray(outStream);
			fo.write(bytes);
			fo.flush();
			fo.close();
			assertTrue("Error downloading file", outputFile.length() > 0);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			om.deleteFile(rootFolderName, keyName);
			om.deleteFile(rootFolderName, folderName);
		}
		
	}

}
