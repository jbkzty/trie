package ik;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>Description: 
 * 
 * 实现分词器的字典树结构
 * 
 * 
 * 如果文字都是英文的话，字典树只有26个分支，但是现在中文的存在，决定看一下IK是如何处理字典树的
 * 
 * </p>
 * 
 * 
 * @author junjin4838
 * @date 2017年1月22日
 * @version 1.0
 */
public class DictSegment {
	
	//公用字典表，用来存储汉字
	private static final Map<Character,Character> charMap = new HashMap<Character,Character>(16,0.95f);
	
	//数组大小的上限
	private static final int ARRAY_LENGTH_LIMIT = 3;
	
	//Map存储结构
	private Map<Character,DictSegment> childrenMap;
	
	//数组方式存储结构
	private DictSegment[] childrenArray;
	
	//当前节点上存储的字符
    private Character nodeChar;
    
    //当前节点存储的Segment数目
    //Segment <= ARRAY_LENGTH_LIMIT 使用数组
    //Segment >  ARRAY_LENGTH_LIMIT 使用Map
    private int storeSize = 0;
    
    //当前DictSegment的状态，默认为0，1表示从根节点到当前节点的路径表示一个词
    private int nodeState = 0;    
    
    /**
     * 构造器
     * @param nodeChar
     */
    public DictSegment(Character nodeChar){
    	
    	if(nodeChar == null){
    		throw new IllegalArgumentException("参数为空异常，字符不能为空");
    	}
    	
    	this.nodeChar = nodeChar;
    }
    
    /**
     * 加载填充词典片段
     */
    private void fillSegment(char[] charArray,int begin,int length,int enable){
    	
    	//获取字典表中的汉字对象
    	Character beginChar = new Character(charArray[begin]);
    	
    	Character keyChar = charMap.get(beginChar);
    	
    	//字典中没有这个字的话，则加入到词典
    	if(keyChar == null){
    		charMap.put(beginChar, beginChar);
    		keyChar = beginChar;
    	}
    	
    	//搜索当前的节点存储
    	DictSegment ds = lookforSegment(keyChar,enable);
    	if(ds != null){
    		//处理keyChar对应的segment
    		if(length > 1){
    			ds.fillSegment(charArray, begin+1, length-1, enable);
    		}else if(length ==1){
    			//已经是词元的最后一个char，设置当前节点状态为enable
    			ds.nodeState = enable;
    		}
    	}
    }
    
    /**
     * 查找本节点下对应的keyChar的segment
     * @param keyChar
     * @param create 
     *           1 - 如果没有找到，则创建新的segment
     *           0 - 如果没有找到，则不创建，返回null
     * @return
     */
    private DictSegment lookforSegment(Character keyChar,int create){
    	
    	DictSegment ds = null;
    	
    	if(this.storeSize <= ARRAY_LENGTH_LIMIT){
    		
    		//获取数组容器，如果数组未创建便创建数组
    		DictSegment[] segmentArray = getChildrenArray();
    		
    		//搜索数组
    		DictSegment keySegment = new DictSegment(keyChar);
    		int position = Arrays.binarySearch(segmentArray, 0,this.storeSize,keySegment);
    		
    		if(position >=0){
    			ds = segmentArray[position];
    		}
    		
    		//遍历数组之后没有找到对应的segment
    		if(ds == null && create == 1){
    			ds = keySegment;
    			if(this.storeSize < ARRAY_LENGTH_LIMIT){
    				//数组容量未满，使用数组来存储
    				segmentArray[this.storeSize] = ds;
    				this.storeSize++;
    				Arrays.sort(segmentArray, 0,this.storeSize);
    			}else{
    				//数组容量已满，切换Map来存储
    				//获取Map容器，如果Map未创建，则创建Map
    				Map<Character,DictSegment> segmentMap = getChildrenMap();
    				//将数组中的segment迁移到Map中
    				migrate(segmentArray,segmentMap);
    				//存储新的segment
    				segmentMap.put(keyChar, ds);
    				//segment数目+1，必须在释放数组前执行stroeSize++；确保在极端的情况下，不会取到空的数组
    				this.storeSize ++;
    				this.childrenArray = null;
    			}
    		}
    		
    	}else{
    		
    		//获取Map容器，如果Map未创建，则创建Map
			Map<Character,DictSegment> segmentMap = getChildrenMap();
			//搜索Map
			ds = segmentMap.get(keyChar);
			if(ds == null && create ==1){
				ds = new DictSegment(keyChar);
				segmentMap.put(keyChar, ds);
				this.storeSize ++;
			}
    	}
    	
    	return ds;
    }
    
    
    /**
     * 获取数组容器
     * 线程同步的方法
     * @return
     */
    private DictSegment[] getChildrenArray(){
    	
    	if(this.childrenArray == null){
    		synchronized (this) {
				if(this.childrenArray == null){
					this.childrenArray = new DictSegment[ARRAY_LENGTH_LIMIT];
				}
			}
    	}
    	return this.childrenArray;
    }
    
    /**
     * 获取Map容器
     * 线程同步的方法
     * @return
     */
    private Map<Character,DictSegment> getChildrenMap(){
    	
    	if(this.childrenMap == null){
    		synchronized (this) {
				if(this.childrenMap == null){
					this.childrenMap = new HashMap<Character,DictSegment>(ARRAY_LENGTH_LIMIT * 2,0.8f);
				}
			}
    	}
    	
    	return this.childrenMap;
    }
    
    
    /**
     * 将数组中的segment迁移到Map中
     * @param segmentArray
     * @param segmentMap
     */
    private void migrate(DictSegment[] segmentArray,Map<Character,DictSegment> segmentMap){
    	for (DictSegment segment : segmentArray) {
    		if(segment != null){
    			segmentMap.put(segment.nodeChar, segment);
    		}
		}
    	
    }

}
