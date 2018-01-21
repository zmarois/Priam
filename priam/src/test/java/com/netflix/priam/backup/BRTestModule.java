package com.netflix.priam.backup;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.netflix.priam.*;
import com.netflix.priam.aws.S3BackupPath;
import com.netflix.priam.compress.ICompression;
import com.netflix.priam.compress.SnappyCompression;
import com.netflix.priam.cryptography.IFileCryptography;
import com.netflix.priam.cryptography.pgp.PgpCryptography;
import com.netflix.priam.defaultimpl.CassandraProcessManager;
import com.netflix.priam.identity.IMembership;
import com.netflix.priam.identity.IPriamInstanceFactory;
import com.netflix.priam.identity.token.*;
import com.netflix.priam.utils.FakeSleeper;
import com.netflix.priam.utils.ITokenManager;
import com.netflix.priam.utils.Sleeper;
import com.netflix.priam.utils.TokenManager;
import org.junit.Ignore;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Arrays;

@Ignore
public class BRTestModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(IConfiguration.class)
                .toInstance(new FakeConfiguration(FakeConfiguration.FAKE_REGION, "fake-app", "az1", "fakeInstance1"));
        bind(IPriamInstanceFactory.class).to(FakePriamInstanceFactory.class);
        bind(SchedulerFactory.class).to(StdSchedulerFactory.class).in(Scopes.SINGLETON);
        bind(IMembership.class).toInstance(new FakeMembership(Arrays.asList("fakeInstance1")));
        bind(ICredential.class).to(FakeNullCredential.class).in(Scopes.SINGLETON);
        //        bind(IBackupFileSystem.class).to(FakeBackupFileSystem.class).in(Scopes.SINGLETON);
        bind(IBackupFileSystem.class).annotatedWith(Names.named("backup")).to(FakeBackupFileSystem.class)
                .in(Scopes.SINGLETON);
        bind(IBackupFileSystem.class).annotatedWith(Names.named("incr_restore")).to(FakeBackupFileSystem.class)
                .in(Scopes.SINGLETON);
        bind(AbstractBackupPath.class).to(S3BackupPath.class);
        bind(ICompression.class).to(SnappyCompression.class);
        bind(Sleeper.class).to(FakeSleeper.class);
        bind(ITokenManager.class).to(TokenManager.class);
        bind(ICassandraProcess.class).to(CassandraProcessManager.class);

        bind(IDeadTokenRetriever.class).to(DeadTokenRetriever.class);
        bind(IPreGeneratedTokenRetriever.class).to(PreGeneratedTokenRetriever.class);
        bind(INewTokenRetriever.class)
                .to(NewTokenRetriever.class); //for backward compatibility, unit test always create new tokens

        bind(IFileSystemContext.class).annotatedWith(Names.named("backup")).to(BackupFileSystemContext.class);
        bind(IBackupFileSystem.class).annotatedWith(Names.named("encryptedbackup"))
                .to(FakedS3EncryptedFileSystem.class);
        bind(IFileCryptography.class).annotatedWith(Names.named("filecryptoalgorithm")).to(PgpCryptography.class);

    }
}