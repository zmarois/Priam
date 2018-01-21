/**
 * Copyright 2017 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.priam.restore;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.netflix.priam.ICassandraProcess;
import com.netflix.priam.IConfiguration;
import com.netflix.priam.ICredentialGeneric;
import com.netflix.priam.aws.S3CrossAccountFileSystem;
import com.netflix.priam.backup.AbstractBackupPath;
import com.netflix.priam.backup.MetaData;
import com.netflix.priam.compress.ICompression;
import com.netflix.priam.cryptography.IFileCryptography;
import com.netflix.priam.health.InstanceState;
import com.netflix.priam.identity.InstanceIdentity;
import com.netflix.priam.scheduler.SimpleTimer;
import com.netflix.priam.scheduler.TaskTimer;
import com.netflix.priam.utils.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * A strategy to restore from an AWS bucket whose objects are not owned by the current IAM role thus requiring AWS cross account assumption.
 * In addition, this strategy will handle data which has been encrypted.
 */

@Singleton
public class AwsCrossAccountCryptographyRestoreStrategy extends EncryptedRestoreBase
{
    public static final String JOBNAME = "AWS_CROSS_ACCT_CRYPTOGRAPHY_RESTORE_JOB";
    private static final Logger logger = LoggerFactory.getLogger(AwsCrossAccountCryptographyRestoreStrategy.class);

    //Note: see javadoc for S3CrossAccountFileSystem for reason why we inject a concrete class (S3CrossAccountFileSystem) instead of the inteface IBackupFileSystem
    @Inject
    public AwsCrossAccountCryptographyRestoreStrategy(final IConfiguration config, ICassandraProcess cassProcess
            , S3CrossAccountFileSystem crossAcctfs
            , Sleeper sleeper
            , @Named("filecryptoalgorithm") IFileCryptography fileCryptography
            , @Named("pgpcredential") ICredentialGeneric credential
            , ICompression compress, Provider<AbstractBackupPath> pathProvider,
            InstanceIdentity id, RestoreTokenSelector tokenSelector, MetaData metaData, InstanceState instanceState)
    {

        super(config, crossAcctfs.getBackupFileSystem(), JOBNAME, sleeper, cassProcess, pathProvider, id, tokenSelector,
                credential, fileCryptography, compress, metaData, instanceState);
    }

    /**
     * @return a timer used by the scheduler to determine when "this" should be run.
     */
    public static TaskTimer getTimer()
    {
        return new SimpleTimer(JOBNAME);
    }
}