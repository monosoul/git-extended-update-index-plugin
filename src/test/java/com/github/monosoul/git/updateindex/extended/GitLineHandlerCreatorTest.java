package com.github.monosoul.git.updateindex.extended;

import static com.intellij.openapi.application.ApplicationManager.setApplication;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.config.GitExecutableManager;
import git4idea.config.GitVersion;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.picocontainer.PicoContainer;

class GitLineHandlerCreatorTest {

    private static final int LIMIT = 10;
    private static GitVersion CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND = new GitVersion(1, 7, 1, 0);

    @Mock
    private Application application;
    @Mock
    private Disposable parent;
    @Mock
    private PicoContainer picoContainer;
    @Mock
    private GitExecutableManager gitExecutableManager;
    @Mock
    private ProjectLevelVcsManager vcsManager;
    @Mock
    private GitVcs gitVcs;
    @Mock
    private Project project;
    @Mock
    private ExtendedUpdateIndexCommand updateIndexCommand;

    @BeforeEach
    void setUp() {
        initMocks(this);
        setApplication(application, parent);

        doReturn(picoContainer)
                .when(application).getPicoContainer();
        doReturn(gitExecutableManager)
                .when(picoContainer).getComponentInstance(GitExecutableManager.class.getName());
        doReturn(randomAlphabetic(LIMIT))
                .when(gitExecutableManager).getPathToGit(project);
        doReturn(vcsManager)
                .when(project).getComponent(ProjectLevelVcsManager.class);
        doReturn(gitVcs)
                .when(vcsManager).findVcsByName(GitVcs.NAME);
        doReturn(CAN_NOT_OVERRIDE_GIT_CONFIG_FOR_COMMAND)
                .when(gitVcs).getVersion();
    }

    @ParameterizedTest
    @MethodSource("entryStream")
    void apply(final Entry<VirtualFile, List<VirtualFile>> entry) {
        val skipWorkTreeCommandString = randomAlphabetic(LIMIT);

        when(updateIndexCommand.getCommand()).thenReturn(skipWorkTreeCommandString);

        val handler = new GitLineHandlerCreator(project, updateIndexCommand).apply(entry);
        val expected = buildExpected(skipWorkTreeCommandString, entry.getValue());

        System.out.println(handler.printableCommandLine());
        assertThat(handler.printableCommandLine()).isEqualTo(expected);
    }

    private static String buildExpected(final String commandString, final List<VirtualFile> files) {
        return "git update-index " + commandString + " " + files.stream().map(VirtualFile::getName).collect(joining(" "));
    }

    @SuppressWarnings("unchecked")
    private static Stream<Entry<VirtualFile, List<VirtualFile>>> entryStream() {
        return generate(() -> {
            val root = new MockVirtualFile(true, randomAlphabetic(LIMIT));
            val files = mockVirtualFileStream().map(f -> {
                f.setParent(root);
                return (VirtualFile) f;
            }).collect(toList());

            val entry = (Entry<VirtualFile, List<VirtualFile>>) mock(Entry.class);

            when(entry.getKey()).thenReturn(root);
            when(entry.getValue()).thenReturn(files);

            return entry;
        }).limit(LIMIT);
    }

    private static Stream<MockVirtualFile> mockVirtualFileStream() {
        return generate(() -> new MockVirtualFile(randomAlphabetic(LIMIT))).limit(nextInt(1, LIMIT));
    }
}