
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import plannerOut.Parameter;
import plannerOut.PlannerOutput;
import plannerOut.Value;
import provisionerIn.Eth;
import provisionerIn.SubTopology;
import provisionerIn.SubTopologyInfo;
import provisionerIn.Subnet;
import provisionerIn.TopTopology;
import provisionerIn.VM;
import provisionerIn.EC2.EC2VM;
import provisionerIn.EGI.EGIVM;
import provisionerIn.EC2.EC2SubTopology;
import provisionerIn.EGI.EGISubTopology;
import provisionerIn.ExoGENI.ExoGENISubTopology;
import provisionerIn.ExoGENI.ExoGENIVM;


public class P2PConverter {

	public static SimplePlanContainer transfer(String plannerOutputJson, String userName, String OStype, String domainName, String clusterType, String cloudProvider) throws JsonParseException, JsonMappingException, IOException{
		
		Parameter plannerOutput = getInfoFromPlanner(plannerOutputJson);
		
		TopTopology topTopology = new TopTopology();
		SubTopology subTopology = null;
		if(cloudProvider.trim().toLowerCase().equals("ec2"))
			subTopology = new EC2SubTopology();
		else if(cloudProvider.trim().toLowerCase().equals("egi"))
			subTopology = new EGISubTopology();
		else if(cloudProvider.trim().toLowerCase().equals("exogeni"))
			subTopology = new ExoGENISubTopology();
		else{
			System.out.println("The "+cloudProvider+" is not supported yet!");
			return null;
		}
		SubTopologyInfo sti = new SubTopologyInfo();
		
		sti.cloudProvider = cloudProvider;
		sti.topology = UUID.randomUUID().toString();
		sti.domain = domainName;
		sti.status = "fresh";
		sti.tag = "fixed";
		
		
		topTopology.publicKeyPath = null;
		topTopology.userName = userName;
		
		
		if(cloudProvider.trim().toLowerCase().equals("ec2")){
			Subnet s = new Subnet();
			s.name = "s1";
			s.subnet = "192.168.10.0";
			s.netmask = "255.255.255.0";
			subTopology.subnets = new ArrayList<Subnet>();
			subTopology.subnets.add(s);
		}
		
		if(cloudProvider.trim().toLowerCase().equals("ec2"))
			((EC2SubTopology)subTopology).components = new ArrayList<EC2VM>();
		else if(cloudProvider.trim().toLowerCase().equals("egi"))
			((EGISubTopology)subTopology).components = new ArrayList<EGIVM>();
		else if(cloudProvider.trim().toLowerCase().equals("exogeni"))
			((ExoGENISubTopology)subTopology).components = new ArrayList<ExoGENIVM>();
		else{
			System.out.println("The "+cloudProvider+" is not supported yet!");
			return null;
		}
		
		
		boolean firstVM = true;
		for(int vi = 0 ; vi < plannerOutput.value.size() ; vi++){
			Value curValue = plannerOutput.value.get(vi);
			VM curVM = null;
			if(cloudProvider.trim().toLowerCase().equals("ec2"))
				curVM = new EC2VM();
			else if(cloudProvider.trim().toLowerCase().equals("egi"))
				curVM = new EGIVM();
			else{
				System.out.println("The "+cloudProvider+" is not supported yet!");
				return null;
			}
			curVM.name = curValue.name;
			curVM.type = "Switch.nodes.Compute";
			curVM.OStype = OStype;
			curVM.clusterType = clusterType;
			curVM.dockers = curValue.docker;
			
			if(cloudProvider.trim().toLowerCase().equals("ec2")){
				if(curValue.size.trim().toLowerCase().equals("small"))
					curVM.nodeType = "t2.small";
				else if(curValue.size.trim().toLowerCase().equals("medium"))
					curVM.nodeType = "t2.medium";
				else if(curValue.size.trim().toLowerCase().equals("large"))
					curVM.nodeType = "t2.large";
				else{
					throw new IllegalArgumentException("Invalid value for field 'size' in input JSON String");
				}
				
				Eth eth = new Eth();
				eth.name = "p1";
				eth.subnetName = "s1";
				int hostNum = 10+vi;
				String priAddress = "192.168.10."+hostNum;
				eth.address = priAddress;
				curVM.ethernetPort = new ArrayList<Eth>();
				curVM.ethernetPort.add(eth);
			}
			
			if(cloudProvider.trim().toLowerCase().equals("egi")){
				if(curValue.size.trim().toLowerCase().equals("small"))
					curVM.nodeType = "small";
				else if(curValue.size.trim().toLowerCase().equals("medium"))
					curVM.nodeType = "medium";
				else if(curValue.size.trim().toLowerCase().equals("large"))
					curVM.nodeType = "large";
				else{
					throw new IllegalArgumentException("Invalid value for field 'size' in input JSON String");
				}
			}
			
			if(firstVM){
				curVM.role = "master";
				firstVM = false;
			}else
				curVM.role = "slave";
			
			if(cloudProvider.trim().toLowerCase().equals("ec2"))
				((EC2SubTopology)subTopology).components.add((EC2VM)curVM);
			else if(cloudProvider.trim().toLowerCase().equals("egi"))
				((EGISubTopology)subTopology).components.add((EGIVM)curVM);
			else{
				System.out.println("The "+cloudProvider+" is not supported yet!");
				return null;
			}
		}
		
		sti.subTopology = subTopology;
		
		topTopology.topologies = new ArrayList<SubTopologyInfo>();
		topTopology.topologies.add(sti);
		
		SimplePlanContainer spc = generateInfo(topTopology);
		
		return spc;
	}
	
	private static Parameter getInfoFromPlanner(String json) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		PlannerOutput po = mapper.readValue(json, PlannerOutput.class);
		System.out.println("");
		return po.parameters.get(0);
	}
	
	private static SimplePlanContainer generateInfo(TopTopology topTopology) throws JsonProcessingException{
		SimplePlanContainer spc = new SimplePlanContainer();
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		
    	String yamlString = mapper.writeValueAsString(topTopology);
    	spc.topLevelContents = yamlString.substring(4);
    	
    	Map<String, String> output = new HashMap<String, String>();
    	for(int i = 0 ; i<topTopology.topologies.size() ; i++){
    		String key = topTopology.topologies.get(i).topology;
    		String value = mapper.writeValueAsString(topTopology.topologies.get(i).subTopology);
    		output.put(key, value.substring(4));
    	}
    	
    	spc.lowerLevelContents = output;
    	
    		
		return spc;
	}

}
