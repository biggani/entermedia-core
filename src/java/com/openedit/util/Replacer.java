package com.openedit.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.openedit.Data;
import org.openedit.data.SearcherManager;

import com.openedit.WebPageRequest;


public class Replacer
{
	protected SearcherManager fieldSearcherManager;
	protected boolean fieldAlwaysReplace =  false;
	protected String fieldCatalogId;
	
	public boolean isAlwaysReplace() {
		return fieldAlwaysReplace;
	}

	public void setAlwaysReplace(boolean inAlwaysReplace) {
		fieldAlwaysReplace = inAlwaysReplace;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inDefaultCatalogId)
	{
		fieldCatalogId = inDefaultCatalogId;
	}


	public String replace(String inCode, Data inValues)
	{
		return replace(inCode, inValues.getProperties() );
	}

	
	public String replace(String inCode, Map<String, Object> inValues)
	{
		if( inCode == null)
		{
			return inCode;
		}
		if(inValues == null){
			return inCode;	
		}
		int start = 0;
		while( (start = inCode.indexOf("${",start)) != -1)
		{
			int end = inCode.indexOf("}",start);			
			if( end == -1)
			{
				break;
			}
			
			String key = inCode.substring(start+2,end);
			Object variable = null;
			ArrayList<String> values = findKeys(key,"||");
			
			for(String value:values)
			{
				variable = inValues.get(value); //check for property
				if( variable == null )
				{
					//TODO: Loop over each of the dots to find the final object
					int dot = value.indexOf('.');
					if( dot > 0)
					{
						String objectname = value.substring(0,dot);
						
						Object object = inValues.get(objectname);  //${division.folder}
						if( object instanceof String )
						{
							object = getData(objectname,(String)object); //division
							if(isAlwaysReplace() && object == null)
							{
								variable="";
							}
						}
						if(object instanceof Data)
						{
							Data data = (Data)object;
							String method = value.substring(dot+1);
							variable = data.get(method);
						}
					}
				}
				if (variable!=null){
					break;
				}
			}
			
			if( isAlwaysReplace() && variable == null )
			{
				variable="";
			}
			
			
			if( variable != null)
			{
				String sub = variable.toString();
				sub = replace(sub,inValues);
				inCode = inCode.substring(0,start) + sub + inCode.substring(end+1);
				start = start + sub.length();
			}
			else
			{
				start = end; //could not find a hit, go to the next one
			}
		}
		return inCode;
	}
	
	protected ArrayList<String> findKeys(String Subject, String Delimiters) 
    {
		StringTokenizer tok = new StringTokenizer(Subject, Delimiters);
		ArrayList<String> list = new ArrayList<String>(Subject.length());
		while(tok.hasMoreTokens()){
			list.add(tok.nextToken());
		}
		return list;
    }

	protected Data getData(String inType, String inId)
	{
		if( fieldSearcherManager == null )
		{
			return null;
		}
		return getSearcherManager().getData(getCatalogId(), inType, inId);
	}
}
