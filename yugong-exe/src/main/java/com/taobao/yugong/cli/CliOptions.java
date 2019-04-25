package com.taobao.yugong.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * 配置命令
 * @author chaijunkun
 */
public class CliOptions {

    private Option createOptionHelp(){
        return Option.builder(ArgDef.help.getShortName())
                .longOpt(ArgDef.help.getLongName())
                .desc(ArgDef.help.getDesc())
                .build();
    }

    private Option createOptionConfig() {
        return Option.builder(ArgDef.config.getShortName())
                .longOpt(ArgDef.config.getLongName())
                .hasArg()
                .argName(ArgDef.config.getArgVal())
                .desc(ArgDef.config.getDesc())
                .build();
    }

    private Option createOptionYaml() {
        return Option.builder(ArgDef.yaml.getShortName())
                .longOpt(ArgDef.yaml.getLongName())
                .hasArg()
                .argName(ArgDef.yaml.getArgVal())
                .desc(ArgDef.yaml.getDesc())
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

        cliOptions.getOpts().addOption(cliOptions.createOptionHelp());
        cliOptions.getOpts().addOption(cliOptions.createOptionConfig());
        cliOptions.getOpts().addOption(cliOptions.createOptionYaml());

        return cliOptions;
    }


}
