package com.example.vidish.insertdata;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vidish on 23-10-2016.
 */
public class SubjectJSONParser {
    public SubjectJSONParser()
    {}
    public static List<String> extractSubjects(String json)
    {
        List<String> subjects=new ArrayList<>();
        try {
            JSONObject root=new JSONObject(json);
            JSONArray result=root.optJSONArray("result");
            if(result.length() == 0 || result == null)
                return subjects;
            for(int i=0;i<result.length();i++)
            {
                JSONObject object=result.getJSONObject(i);
                String col=object.getString("col");
                if(col!=null && !col.equals("null")) {
                    subjects.add(col);
                }
            }
        } catch (JSONException e) {
            Log.v("JSONParser","exception",e);
        }
        return subjects;
    }
}