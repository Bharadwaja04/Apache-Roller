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
 * limitations under the License.
 */

package org.apache.roller.weblogger.pojos;

import java.util.List;

/**
 * Service for managing category position/ordering.
 * Extracted from WeblogCategory to address Cyclic-Dependent Modularization.
 * 
 * <p>This service handles position calculation logic that was
 * previously embedded in the WeblogCategory POJO.</p>
 */
public class CategoryPositionService {

    /**
     * Calculate position for a new category in a weblog.
     * 
     * @param weblog The weblog
     * @return The position for a new category
     */
    public int calculateNewPosition(Weblog weblog) {
        if (weblog == null) {
            return 0;
        }
        
        List<WeblogCategory> categories = weblog.getWeblogCategories();
        if (categories == null || categories.isEmpty()) {
            return 0;
        }
        
        int size = categories.size();
        if (size == 1) {
            return 0;
        }
        
        return categories.get(size - 1).getPosition() + 1;
    }

    /**
     * Calculate and set position for a category.
     * 
     * @param category The category to position
     */
    public void assignPosition(WeblogCategory category) {
        if (category == null || category.getWeblog() == null) {
            return;
        }
        
        Weblog weblog = category.getWeblog();
        List<WeblogCategory> categories = weblog.getWeblogCategories();
        
        int size = categories.size();
        if (size == 0 || size == 1) {
            category.setPosition(0);
        } else {
            // Get position of second-to-last category and add 1
            category.setPosition(categories.get(size - 2).getPosition() + 1);
        }
    }

    /**
     * Move a category to a new position.
     * 
     * @param category The category to move
     * @param newPosition The target position
     */
    public void moveToPosition(WeblogCategory category, int newPosition) {
        if (category == null || newPosition < 0) {
            return;
        }
        category.setPosition(newPosition);
    }

    /**
     * Swap positions of two categories.
     * 
     * @param category1 First category
     * @param category2 Second category
     */
    public void swapPositions(WeblogCategory category1, WeblogCategory category2) {
        if (category1 == null || category2 == null) {
            return;
        }
        
        int temp = category1.getPosition();
        category1.setPosition(category2.getPosition());
        category2.setPosition(temp);
    }

    /**
     * Move category up one position.
     * 
     * @param category The category to move
     * @param allCategories All categories in the weblog (sorted by position)
     */
    public void moveUp(WeblogCategory category, List<WeblogCategory> allCategories) {
        if (category == null || allCategories == null || allCategories.size() < 2) {
            return;
        }
        
        int currentIndex = allCategories.indexOf(category);
        if (currentIndex > 0) {
            WeblogCategory previous = allCategories.get(currentIndex - 1);
            swapPositions(category, previous);
        }
    }

    /**
     * Move category down one position.
     * 
     * @param category The category to move
     * @param allCategories All categories in the weblog (sorted by position)
     */
    public void moveDown(WeblogCategory category, List<WeblogCategory> allCategories) {
        if (category == null || allCategories == null || allCategories.size() < 2) {
            return;
        }
        
        int currentIndex = allCategories.indexOf(category);
        if (currentIndex < allCategories.size() - 1) {
            WeblogCategory next = allCategories.get(currentIndex + 1);
            swapPositions(category, next);
        }
    }

    /**
     * Renumber all category positions sequentially.
     * 
     * @param categories Categories to renumber (should be sorted by desired order)
     */
    public void renumberPositions(List<WeblogCategory> categories) {
        if (categories == null) {
            return;
        }
        
        for (int i = 0; i < categories.size(); i++) {
            categories.get(i).setPosition(i);
        }
    }
}