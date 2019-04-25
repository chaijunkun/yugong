package com.taobao.yugong;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.taobao.yugong.cli.ArgDef;
import com.taobao.yugong.cli.CliOptions;
import com.taobao.yugong.common.version.VersionInfo;
import com.taobao.yugong.conf.YugongConfiguration;
import com.taobao.yugong.controller.YuGongController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;

/**
 * Single jar app
 * @author dijingchao
 */
@Slf4j
public class YugongApp {

    private static YAMLMapper yamlMapper = new YAMLMapper();

    public static void showHelp(Options opts) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setSyntaxPrefix("用法:");
        helpFormatter.printHelp("startup_cmd", opts, true);
    }

    public static void showHelpWithExitCode(Options opts, int code) {
        showHelp(opts);
        System.exit(code);
    }


    public static void main(String[] args) {
        Options opts = CliOptions.getNewInstance().getOpts();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cl = parser.parse(opts, args);
            do {
                if (ArrayUtils.isEmpty(args)) {
                    throw new IllegalArgumentException("please specific essential args, or -h to get help information");
                }
                if (cl.hasOption(ArgDef.help.getShortName())) {
                    showHelp(opts);
                    break;
                }

                // 加载config
                PropertiesConfiguration config = new PropertiesConfiguration();
                if (cl.hasOption(ArgDef.config.getShortName())) {
                    config.load(new File(cl.getOptionValue(ArgDef.config.getShortName())));
                } else {
                    throw new ConfigurationException(String.format("no required parameter:%s", ArgDef.config.getShortName()));
                }

                // 加载 yaml
                YugongConfiguration yugongConfiguration;
                if (cl.hasOption(ArgDef.yaml.getShortName())) {
                    yugongConfiguration = yamlMapper.readValue(new File(cl.getOptionValue(ArgDef.yaml.getShortName())), YugongConfiguration.class);
                } else {
                    throw new IOException(String.format("no required parameter:%s", ArgDef.yaml.getShortName()));
                }

                try {
                    run(config, yugongConfiguration);
                } catch (Throwable e) {
                    log.error("## Something goes wrong when starting up the YuGong:\n{}", ExceptionUtils.getFullStackTrace(e));
                }

            } while (false);
        } catch (ParseException parseExp) {
            log.error("命令行错误:{}", parseExp.getMessage());
            showHelpWithExitCode(opts, 500);
        } catch (ConfigurationException e) {
            log.error("Configuration load error", e);
            showHelpWithExitCode(opts, 100);
        } catch (IOException e) {
            log.error("YAML configuration load error", e);
            showHelpWithExitCode(opts, 200);
        } catch (IllegalArgumentException e) {
            log.error("argument error, {}", e.getMessage());
            showHelpWithExitCode(opts, 1);
        }

    }


    private static void run(PropertiesConfiguration config,
                            YugongConfiguration yugongConfiguration) throws InterruptedException {
        final YuGongController controller = new YuGongController(config, yugongConfiguration);
        log.info("## start the YuGong.");
        controller.start();
        log.info("## the YuGong is running now ......");
        log.info(VersionInfo.getBuildVersion());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!controller.isStart()) {
                return;
            }
            try {
                log.info("## Stop the YuGong");
                controller.stop();
            } catch (Throwable e) {
                log.warn("## Something goes wrong when stopping YuGong:\n{}",
                        ExceptionUtils.getFullStackTrace(e));
            } finally {
                log.info("## YuGong is down.");
            }
        }));
        // 如果所有都完成，则进行退出
        controller.waitForDone();
        // 等待3s，清理上下文
        Thread.sleep(3 * 1000);
        log.info("## stop the YuGong");
        if (controller.isStart()) {
            controller.stop();
        }
        log.info("## YuGong is down.");
    }
}
