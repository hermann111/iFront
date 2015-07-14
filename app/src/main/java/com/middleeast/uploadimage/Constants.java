/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.middleeast.uploadimage;

public class Constants {
    // You should replace these values with your own
    // See the readme for details on what to fill in
    public static final String AWS_ACCOUNT_ID = "949358403867";
    public static final String COGNITO_POOL_ID ="us-east-1:d06492fb-71ad-49a4-b789-6ad4661b7709";
    public static final String COGNITO_ROLE_UNAUTH ="arn:aws:iam::949358403867:role/Cognito_satwant11111Unauth_DefaultRole";
    // Note, the bucket will be created in all lower case letters
    // If you don't enter an all lower case title, any references you add
    // will need to be sanitized
    public static final String BUCKET_NAME = "ishmeet11111";

    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME = "com.middleeast.uploadimage";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
}
