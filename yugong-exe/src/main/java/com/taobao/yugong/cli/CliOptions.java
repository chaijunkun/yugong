package com.taobao.yugong.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * 配置命令
 * @author chaijunkun
 */
public class CliOptions {

    public static final String ARG_HELP = "h";

    public static final String ARG_CONFIG = "c";

    public static final String ARG_YAML = "y";

    private Option createOptionHelp(){
        return Option.builder(ARG_HELP)
                .desc("获取帮助信息")
                .build();
    }

    private Option createOptionConfig() {
        return Option.builder(ARG_CONFIG)
                .hasArg()
                .argName("config")
                .desc("指定配置文件")
                .build();
    }

    private Option createOptionYaml() {
        return Option.builder(ARG_YAML)
                .hasArg()
                .argName("yaml")
                .desc("指定yaml配置文件")
                .build();
    }

    private Options opts;

    private CliOptions(){
        opts = new Options();
    }

    public Options getOpts(){
        return this.opts;
    }

    public static CliOptions getNewInstance(){
        CliOptions cliOptions = new CliOptions();
//        OptionGroup optQueryOrModeGrp = new OptionGroup();
//        optQueryOrModeGrp.addOption(cliOptions.createOptionMode());
//        cliOptions.getOpts().addOptionGroup(optQueryOrModeGrp);

        cliOptions.getOpts().addOption(cliOptions.createOptionConfig());
        cliOptions.getOpts().addOption(cliOptions.createOptionYaml());

        cliOptions.getOpts().addOption(cliOptions.createOptionHelp());
        return cliOptions;
    }


}
