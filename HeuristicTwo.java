package com.company;



import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HeuristicTwo{
	protected int numberOfDropBox;//Number of DropBox
    protected int numberOfNodes;//Number of Nodes
    protected double sum,minSum=0.0;//Variables to define Sum and MinSum i.e. cost
    protected int mules;//Number of Mules
    protected int numberOfCenters;//Number of centers same as number of towers
    protected double [][]pathWayMatrix;//Array to hold the distance between the nodes
    protected int towerRange;//Variable to hold the tower range
    //Method to get the cost of the clusters
    public double getSum(){
        return sum;
    }
    //Method to get the minimum cost of the clusters
    public double getMinSum(){
        return minSum;
    }
    //Method to get the required values
    public void getValues(){
        Scanner scanner=new Scanner(System.in);
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("/home/swaraj/Public/Java/input.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject jobj = (JSONObject)parser.parse(new FileReader("/home/swaraj/Public/Java/api_output.json"));
            JSONArray resourceSets = (JSONArray) jobj.get("resourceSets");
            JSONObject jsonObject1 = (JSONObject) resourceSets.get(0);
            JSONArray resources = (JSONArray) jsonObject1.get("resources");
            JSONObject jsonObject2 = (JSONObject) resources.get(0);
            JSONArray pathWayList = (JSONArray) jsonObject2.get("results");
            long mules = (long) jsonObject.get("Mules");
            long numberOfDropBox = (long) jsonObject.get("Dropbox");
            long numberOfNodes = (long) jsonObject.get("Dropbox");
            long numberOfCenters = (long) jsonObject.get("Towers");
            long towerRange = (long) jsonObject.get("Range");
            this.mules=(int)mules;
            this.numberOfDropBox=(int)numberOfDropBox;
            this.numberOfNodes=(int)numberOfNodes;
            this.numberOfCenters=(int)numberOfCenters;
            this.towerRange=(int)towerRange;
            pathWayMatrix=new double[this.numberOfNodes+1][this.numberOfNodes+1];
            for(int i=0;i<pathWayList.size();i++){
                JSONObject temp = (JSONObject) pathWayList.get(i);
                long a = (long) temp.get("originIndex");
                long b = (long) temp.get("destinationIndex");
                Object object = temp.get("travelDistance");
                if(object.getClass().getName().equals("java.lang.Long")){
                    pathWayMatrix[(int)a+1][(int)b+1] = (long) object;
                }else{
                    pathWayMatrix[(int)a+1][(int)b+1] = (double) object;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Method to get random number in a given range
    public int getRandomNumber(int lowerBound,int upperBound){
        return ThreadLocalRandom.current().nextInt(1, numberOfNodes);
    }
    //Method to get the initial centers of the clusters
    public int[] getTheInitialClusters(){
        HashMap<Integer,Boolean> alloted = new HashMap<>();
        int []initialCenters=new int[numberOfCenters+1];
        Arrays.fill(initialCenters,0);
        initialCenters[1]=getRandomNumber(1,numberOfNodes);
        alloted.put(initialCenters[1],true);
        int i=2,x=0;
        while(true){
            x=getRandomNumber(1,numberOfNodes);
            if(!alloted.containsKey(x)){
                if(i==numberOfCenters+1){
                    break;
                }
                initialCenters[i]=x;
                i++;
                alloted.put(x,true);
            }
        }
        return initialCenters;
    }
    //Method to assign the initial centers of the clusters
    public  DTNNode[] assignTheInitialCenters(int []initialCenters){
        int minIndex=0;
        double cost=0;
        sum=0.0;
        //Array of DTN Nodes created here
        DTNNode []dtnNode=new DTNNode[numberOfNodes+1];
        for(int i=1;i<=numberOfNodes;i++){
            dtnNode[i]=new DTNNode();
        }
        //Clustering done here
        for (int i=1;i<=numberOfNodes;i++){
            double MIN=Integer.MAX_VALUE;
            for(int j=1;j<initialCenters.length;j++){
                if(pathWayMatrix[i][initialCenters[j]]<MIN){
                    MIN=pathWayMatrix[i][initialCenters[j]];
                    cost=MIN;
                    minIndex=initialCenters[j];
                }
            }
            sum+=cost;//Adding the total cost of the cluster
            dtnNode[i].setNodeNumber(i);//Setting up the required values to the object created
            dtnNode[i].setClusterId(minIndex);
            dtnNode[i].setDistanceFromMedoid(MIN);
        }
        return dtnNode;
    }
    //Method to display the clusters
    public void displayClusters(DTNNode []clusters,int []initialCenters,String message) throws IOException{
        System.out.println(message);
        for(int i=0;i<message.length();i++){
            System.out.print("*");
        }
        System.out.println(" ");
        for(int i=1;i<initialCenters.length;i++){
            if(initialCenters[i]==0)continue;
            System.out.print(initialCenters[i]+"-> [");
            for(int j=1;j<clusters.length;j++){
                if(initialCenters[i]==clusters[j].getClusterId()){
                    System.out.print(j+",");
                }
            }
            System.out.println("\b]\n");
        }
        JSONArray clusterArray = new JSONArray();
        JSONObject obj = new JSONObject();
        if (message.equalsIgnoreCase("Clusters after the tower installed at the group centers")) {
            for (int i = 1; i < initialCenters.length; i++) {
                String str = "";
                if (initialCenters[i] == 0) continue;
                str += initialCenters[i] + "-> [";
                for (int j = 1; j < clusters.length; j++) {
                    if (initialCenters[i] == clusters[j].getClusterId()) {
                        str += j + ",";
                    }
                }
                str = str.substring(0, str.length() - 1);
                str += "]";
                clusterArray.add(str);
            }
            obj.put("Cluster Array: ",clusterArray);
        }
        try (FileWriter file = new FileWriter("/home/swaraj/Public/Java/output2.json")) {
            file.write(obj.toJSONString());
        }
    }
    //Reassigning the clusters to check whether a low cost centers exists in the centers or not
    public void kMedoidAlgorithm(DTNNode []clusters,int []initialCenters) {
        //setTowerRange();
        minSum=sum;
        int []centers=new int[initialCenters.length];
        int []finalCenters=new int[initialCenters.length];
        int k=0;
        //Copying the initial centers to centers array
        for(int i:initialCenters){
            centers[k++]=i;
        }
        //Creating new array of object of DTN nodes
        DTNNode []dtnNodes=new DTNNode[numberOfNodes+1];
        for(int i=1;i<=numberOfNodes;i++){
            dtnNodes[i]=new DTNNode();
        }
        //Array to hold the minimum value of the nodes
        DTNNode []dtnNodesMin=new DTNNode[numberOfNodes+1];
        for(int i=1;i<=numberOfNodes;i++){
            dtnNodesMin[i]=new DTNNode();
        }
        //code to reassign the centers and checking some low cost centers
        for(int i=1;i<initialCenters.length;i++) {
            //centers[i] = initialCenters[i];
            for (int j = 1; j < clusters.length; j++) {
                if (initialCenters[i] == clusters[j].getClusterId()) {
                    //if(pathWayMatrix[initialCenters[i]][dtnNodes[j].getNodeNumber()]<towerRange) {
                        centers[i] = clusters[j].getNodeNumber();
                        dtnNodes = assignTheInitialCenters(centers);
                        if (sum < minSum) {
                            for (int m = 0; m < centers.length; m++) {
                                finalCenters[m] = centers[m];
                            }
                            dtnNodesMin = dtnNodes;
                            minSum = sum;
                        }
                    //}
                }
            }
        }
        try {
            displayClusters(dtnNodesMin, finalCenters, "After applying K Medoid Algorithm clusters are");
        }catch (IOException e){
            System.out.println(e.getStackTrace());
        }
        System.out.println("After applying K Medoid Algorithm cost of the clusters : "+getMinSum());
        assignTowers(dtnNodesMin,finalCenters);
    }
    //Method to Display the Position of Towers
    public void displayTowers(int []towers) throws  IOException{
        String wifi="";
        JSONArray wifiArray=new JSONArray();
        JSONObject obj=new JSONObject();
        for(int i=1;i<=towers.length-1;i++){
            String str="";
            if(towers[i]!=0) {
                System.out.println("wifi_"+i+"="+towers[i]);
                str+="wifi_"+i+"="+towers[i];
                wifiArray.add(str);
                wifi+=str+'\n';
            }
        }
        try(FileWriter fileWriter = new FileWriter("/home/swaraj/Public/Java/InputSettings.txt")){
            fileWriter.write(wifi);
        }
        obj.put("Towers List",wifiArray);
        try (FileWriter file = new FileWriter("/home/swaraj/Public/Java/output1.json")) {
            file.write(obj.toJSONString());
        }
    }
    //Method to get the tower range
    public int getTowerRange(){
        return towerRange;
    }
    //Method to assign the towers according to the range
    public void assignTowers(DTNNode []dtnNodes,int []centers){
        int []towers=new int[centers.length];//Array to hold the tower location
        Arrays.fill(towers,0);
        Arrays.sort(centers);
        towers[1]=centers[1];//Assign the first location as MCS
        //Place towers at the location where it can be placed based on the center of the node
        for(int i=2;i<centers.length;i++){
            for(int j=1;j<centers.length;j++) {
                if (pathWayMatrix[towers[j]][centers[i]] <= getTowerRange() && towers[j] != 0) {
                    towers[i] = centers[i];
                    break;
                }
            }
        }
        //If the towers are not placed check whether other nodes in the group can place the tower or not
        int k;
        for(int j=2;j<centers.length;j++) {
            for (int i = 1; i <dtnNodes.length; i++) {
                if(towers[j]==0){
                    for(k=1;k<centers.length-1;k++){
                        if(towers[k]!=0&& towers[k+1]==0){
                            break;
                        }
                    }
                    if(centers[j]!=i && centers[j]==dtnNodes[i].getClusterId() && pathWayMatrix[towers[k]][i]<=getTowerRange() ){
                        towers[j]=dtnNodes[i].getNodeNumber();//assigning the tower at proper location
                    }

                }
            }
        }
        rearrangeTheClusters(dtnNodes,towers,centers);
    }
    //Method to assign the nodes where the towers are placed
    public void rearrangeTheClusters(DTNNode []dtnNodes,int []towers,int []centers) {
        int countZero=0;
        for(int i=1;i<towers.length;i++){
            if(towers[i]!=0){
                countZero++;
            }
        }
        if(countZero==1){
            //find the minimum range of the towers so that at least two clusters can be there
            double distance =0.0,minDistance=Integer.MAX_VALUE;
            for(int i=2;i<centers.length;i++){
                for(int j=1;j<dtnNodes.length;j++){
                    if(centers[i]==dtnNodes[j].getClusterId()) {
                        distance = pathWayMatrix[towers[1]][dtnNodes[j].getNodeNumber()];
                        if(minDistance>distance){
                            minDistance=distance;
                        }
                    }
                }
            }
            System.out.println("Tower is of very low range for this location.\nTowers should be at least of range "+minDistance+" kms");
        }else {
            try {
                displayTowers(towers);
                for (int i = 1; i < towers.length; i++) {
                    if (towers[i] != centers[i] && towers[i] != 0) {
                        for (int j = 1; j < dtnNodes.length; j++) {
                            if (dtnNodes[j].getClusterId() == centers[i]) {
                                dtnNodes[j].setClusterId(towers[i]);//Moving the node to the group center where there is tower
                            }
                        }
                    }
                }
            }catch(IOException exp){
                System.out.println(exp.getStackTrace());
            }
            reAssignTheNodes(dtnNodes, centers, towers);
        }
        int []clusters=new int[numberOfNodes+1];
        for(int i=1;i<=numberOfNodes;i++){
            clusters[i]=dtnNodes[i].getClusterId();
        }
        try {
            trajectory(towers, clusters);
        }catch(IOException exp){
            System.out.println(exp.getStackTrace());
        }
    }
    public  void trajectory(int[] tower,int[] tempclusters) throws IOException
    {
        int n=tempclusters.length-1;
        int k=tower.length-1;
        Scanner scan=new Scanner(System.in);
        int[] alotted_mules=new int[k+1];
        int divide=(int)Math.floor(mules/k);

        //Finding cost of cluster
        double[] cost=new double[k+1];

        for(int i=1;i<=k;i++)
        {
            for(int j=1;j<=n;j++)
            {
                int pos=tower[i];
                if(tempclusters[j]==pos && j!=pos)
                {
                    cost[i]+=pathWayMatrix[pos][j];
                }
            }
        }

        //finding number of nodes in each cluster
        int[] nodes_in_cluster=new int[k+1];
        for(int i=1;i<=k;i++)
        {
            for(int j=1;j<=n;j++)
            {
                if(tempclusters[j]==tower[i])
                {
                    nodes_in_cluster[i]++;
                }
            }
        }

        //finding highest cost cluster
        double max=0;
        int highest_cost_cluster=0;
        for(int i=1;i<=k;i++)
        {
            if(cost[i]>max)
            {
                max=cost[i];
                highest_cost_cluster=i;
            }
        }

        //System.out.print(highest_cost_cluster);
        int mules1=mules;


        //Proportional mule distribution

        double sum_of_cost=0;
        for(int i=1;i<=k;i++)
        {
            sum_of_cost+=cost[i];
        }

        double[] secendary_cost=new double[k+1];
        double[] secendary_cost_temp=new double[k+1];

        for(int i=1;i<=k;i++)
        {
            secendary_cost[i]=(cost[i]*mules)/sum_of_cost;
            secendary_cost_temp[i]=secendary_cost[i];
        }


        double temp1=0;
        for(int i=1;i<=k;i++)
        {
            for(int j=2;j<=(k-i);j++)
            {
                if(secendary_cost_temp[j-1]<secendary_cost_temp[j])
                {
                    temp1=secendary_cost_temp[j-1];
                    secendary_cost_temp[j-1]=secendary_cost_temp[j];
                    secendary_cost_temp[j]=temp1;
                }
            }
        }

        for(int i=1;i<=k;i++)
        {
            System.out.print(secendary_cost_temp[i]+" ");
        }
        System.out.println();

        for(int i=1;i<=k;i++)
        {
            if(secendary_cost_temp[i]>=1)
            {
                for(int j=1;j<=k;j++)
                {
                    if(secendary_cost_temp[i]==secendary_cost[j])
                    {
                        alotted_mules[j]=(int)Math.floor(secendary_cost[i]);
                        mules1-=alotted_mules[j];
                    }
                }
            }
            else if(secendary_cost_temp[i]==0)
            {
                for(int j=1;j<=k;j++)
                {
                    if(secendary_cost_temp[i]==secendary_cost[j])
                    {
                        alotted_mules[j]=0;
                        mules1-=alotted_mules[j];
                    }
                }
            }
            else
            {
                for(int j=1;j<=k;j++)
                {
                    if(secendary_cost_temp[i]==secendary_cost[j])
                    {
                        alotted_mules[j]=1;
                        mules1-=alotted_mules[j];
                    }
                }
            }
        }
        if(mules1>0)
        {
            alotted_mules[highest_cost_cluster]+=mules1;
        }

        System.out.println("\nPropotional mule distribution as");
        for(int i=1;i<=k;i++)
        {
            System.out.println("Cluster "+i+ "= "+ alotted_mules[i]);
        }
        System.out.println();


        //initial location of mules
        int[] mule_location=new int[mules+1];
        int[] initial_mule_location=new int[mules+1];
        int j=1;

        for(int i=1;i<=k;i++)
        {
            int x=alotted_mules[i];
            while(x>0)
            {
                mule_location[j]=tower[i];
                initial_mule_location[j]=tower[i];
                x--;
                j++;
            }
        }

        System.out.println("Initial mules location");
        for(int i=1;i<=mules;i++)
        {
            System.out.println("mule "+i+" -> "+mule_location[i]);
        }
        System.out.println();

        //finding the trajectories of each mule
        int x=n-k;
        double min=99999.0;
        int index=-1;
        int[] visited=new int[n+1];

        ArrayList<Queue<Integer>> path=new ArrayList<Queue<Integer>>(mules+1);
        for(int i=0;i<=mules;i++)
        {
            Queue<Integer> temp=new LinkedList<Integer>();
            path.add(temp);
        }

        for(int i=1;i<=mules;i++)
        {
            path.get(i).add(initial_mule_location[i]);
        }


        for(int i=1;i<=k;i++)
        {
            visited[tower[i]]=1;
        }

        while(x>0)
        {
            for(j=1;j<=mules;j++)
            {
                if(x<=0)
                    break;

                min=99999.0;
                index=-1;

                for(int i=1;i<=n;i++)
                {
                    if(i!=tempclusters[i] && tempclusters[i]==tempclusters[mule_location[j]] && visited[i]==0 && pathWayMatrix[mule_location[j]][i]<=min)
                    {
                        min=pathWayMatrix[mule_location[j]][i];
                        index=i;
                    }
                }
                if(index!=-1)
                {
                    mule_location[j]=index;
                    //System.out.println(j);
                    path.get(j).add(index);
                    visited[index]=1;
                    x--;
                }
            }

        }

        for(int i=1;i<=mules;i++)
        {
            path.get(i).add(initial_mule_location[i]);
        }
        JSONArray trajectoryArray=new JSONArray();
        JSONObject obj=new JSONObject();
        String trajectory ="";
        for(int i=1;i<=mules;i++)
        {
            String str="";
            System.out.print("DM_"+i+"=");
            str+="DM_"+i+"=";
            for(int p:path.get(i)){
                System.out.print(p+",");
                str+=p+",";
            }
            str=str.substring(0, str.length() - 1);
            System.out.println("\b");
            trajectory+=str+'\n';
            trajectoryArray.add(str);
        }
        obj.put("Trajectory Array",trajectoryArray);
        try(FileWriter fileWriter = new FileWriter("/home/swaraj/Public/Java/InputSettings.txt",true)){
            fileWriter.write(trajectory);
        }
        try (FileWriter file = new FileWriter("/home/swaraj/Public/Java/output3.json")) {
            file.write(obj.toJSONString());
        }
    }
    void reAssignTheNodes(DTNNode []dtnNodes,int []centers,int[] towers){
        double distance=0.0,minDistance=Integer.MAX_VALUE;
        int index=0;
        for(int i=1;i<towers.length;i++){
            if(towers[i]==0){
                for(int j=1;j<dtnNodes.length;j++){
                    if(centers[i]==dtnNodes[j].getClusterId()){
                        for(int k=1;k<towers.length;k++) {
                            if (towers[k] != 0 && k!=i) {
                                distance = pathWayMatrix[towers[k]][j];
                                if (minDistance > distance) {
                                    minDistance = distance;
                                    index = towers[k];
                                }
                            }
                            dtnNodes[j].setClusterId(index);//Assigning the node which is not assigned any tower till now a better cluster according to the distance
                        }
                    }
                }
            }
        }
        try {
            displayClusters(dtnNodes, towers, "Clusters after the tower installed at the group centers");
        }catch (IOException e){
            System.out.println(e.getStackTrace());
        }
    }

    //Main method to call the required functions
    public static void main(String []args){
        HeuristicTwo heuristic1=new HeuristicTwo();
        heuristic1.getValues();
        int []initialCenters=heuristic1.getTheInitialClusters();
        DTNNode []dtnNodes=heuristic1.assignTheInitialCenters(initialCenters);
        try {
            heuristic1.displayClusters(dtnNodes, initialCenters, "Initial Clustering ");
        }catch (IOException e){
            System.out.println(e.getStackTrace());
        }
        System.out.println("Cost of the clusters : "+heuristic1.getSum()+"\n\n");
        heuristic1.kMedoidAlgorithm(dtnNodes,initialCenters);
    }
}
