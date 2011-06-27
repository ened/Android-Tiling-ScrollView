/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * (c) 2011 Sebastian Roth <sebastian.roth@gmail.com>
 */

package asia.ivity.android.tiledscrollview;

import android.app.Application;
import android.content.Context;

/**
 * TiledScrollView Demo application.
 *
 * @author Sebastian Roth <sebastian.roth@gmail.com>
 */
public class TiledScrollViewDemo extends Application {

    public static Context getRealApplicationContext() {
        return mApplicationContext;
    }

    static Context mApplicationContext;


    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationContext = getApplicationContext();
    }
}
