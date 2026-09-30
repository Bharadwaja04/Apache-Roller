/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.pulse;

/**
 * Represents a keyword and its frequency in a conversation.
 */
public class KeywordFrequency implements Comparable<KeywordFrequency> {
    private String keyword;
    private int frequency;
    
    public KeywordFrequency(String keyword, int frequency) {
        this.keyword = keyword;
        this.frequency = frequency;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    @Override
    public String toString() {
        return "KeywordFrequency{" +
                "keyword='" + keyword + '\'' +
                ", frequency=" + frequency +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        KeywordFrequency that = (KeywordFrequency) o;
        
        if (frequency != that.frequency) return false;
        return keyword != null ? keyword.equals(that.keyword) : that.keyword == null;
    }
    
    @Override
    public int hashCode() {
        int result = keyword != null ? keyword.hashCode() : 0;
        result = 31 * result + frequency;
        return result;
    }
    
    @Override
    public int compareTo(KeywordFrequency other) {
        // Sort by frequency descending (higher frequency first)
        return Integer.compare(other.frequency, this.frequency);
    }
}
