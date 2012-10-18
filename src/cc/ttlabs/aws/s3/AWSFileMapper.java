package cc.ttlabs.aws.s3;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.S3Object;

public class AWSFileMapper implements FileMapper {
	
	private static final String SE_END_POINT_START = "s3-";
	private static final String S3_END_POINT_END = ".amazonaws.com";

	private static final String S3_END_POINT_US_STANDARD = "s3.amazonaws.com";
	
	public static final String AWS_REGION_SAOPAULO = "sa-east-1";
	public static final String AWS_REGION_US_STANDARD = "us-east-1";

	private AmazonS3 s3;
	private String region;
	private String endpoint;
	
	@Override
	public boolean isRootFolder(String rootFolderName) {
		return s3.doesBucketExist(rootFolderName);
	}

	public AWSFileMapper(String accessKey, String secretKey, String region) {
		super();
		
        AWSCredentials ac =  new BasicAWSCredentials(accessKey, secretKey);
        
        this.region = region;
        
		s3 = new AmazonS3Client(ac);
		
		if(!region.equals(AWS_REGION_US_STANDARD))
		  setEndpoint(SE_END_POINT_START+region+S3_END_POINT_END);
		else
	      setEndpoint(S3_END_POINT_US_STANDARD);		  
	 
		s3.setEndpoint(getEndpoint());
	}

	@Override
	public void createRootFolder(String rootFolderName) {
		s3.createBucket(rootFolderName, region);
	}

	@Override
	public void deleteRootFolder(String rootFolderName) {
		s3.deleteBucket(rootFolderName);
	}

	@Override
	public void uploadFile(String rootFolderName, String fileName, File file) {
		s3.putObject(rootFolderName, fileName, file);
	}

	@Override
	public void deleteFile(String rootFolderName, String fileName) {
		s3.deleteObject(rootFolderName, fileName);
	}

	@Override
	public void makePublic(String rootFolderName, String fileName) {
		s3.setObjectAcl(rootFolderName, fileName, CannedAccessControlList.PublicRead);
	}

	@Override
	public boolean isFilePublic(String rootFolderName, String fileName) {
		AccessControlList acl = s3.getObjectAcl(rootFolderName, fileName);
		for (Iterator<Grant> iterator = acl.getGrants().iterator(); iterator.hasNext();) {
			Grant grant = iterator.next();
			if(grant.getPermission().equals(Permission.Read) && grant.getGrantee().getIdentifier().equals("http://acs.amazonaws.com/groups/global/AllUsers"))
			  return true;
		}
		return false;
	}

	@Override
	public boolean downloadFile(String rootFolderName, String fileName,
			File outputFile) {
		try{
		s3.getObject(new GetObjectRequest(rootFolderName, fileName), outputFile);
		return true;
		}catch (AmazonServiceException e) {
			e.printStackTrace();
			return false;
		} catch (AmazonClientException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void uploadFile(String rootFolderName, String keyName,
			InputStream stream) {
		s3.putObject(rootFolderName, keyName, stream, null);
	}

	@Override
	public InputStream downloadFile(String rootFolderName, String keyName) {
		S3Object s3Object = s3.getObject(rootFolderName, keyName);
		return s3Object.getObjectContent();
	}

	@Override
	public String getFileUrl(String rootFolderName, String keyName) {
		if(isFilePublic(rootFolderName, keyName))
			return buildPublicFileURL(rootFolderName, keyName);
		else return null;
	}

	private String buildPublicFileURL(String rootFolderName, String keyName) {
		return "http://" + getEndpoint() + "/" + rootFolderName + "/" + keyName;
	}

	private void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getEndpoint() {
		return endpoint;
	}
	
}
