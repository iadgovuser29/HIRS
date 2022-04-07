﻿using CommandLine;
using Serilog;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace hirs {
    class Program {
        public static readonly string VERSION = "14";

        static async Task<int> Main(string[] args) {
            int result = 0;
            try {
                Settings settings = new Settings();
                CLI cli = new CLI();
                Log.Information("Starting hirs version " + VERSION);
                Log.Debug("Parsing CLI args.");
                ParserResult<CLI> cliParseResult =
                    CommandLine.Parser.Default.ParseArguments<CLI>(args)
                        .WithParsed(parsed => cli = parsed)
                        .WithNotParsed(HandleParseError);

                if (cliParseResult.Tag == ParserResultType.NotParsed) {
                    // Help text requested, or parsing failed. Exit.
                    Log.Warning("Could not parse command line arguments. Set --tcp --sim, --tcp <ip>:<port>, --nix, or --win. See documentation for further assistance.");
                } else {
                    Provisioner p = new Provisioner(settings, cli);
                    IHirsAcaTpm tpm = p.connectTpm();
                    p.useClassicDeviceInfoCollector();
                    result = await p.provision(tpm);
                }
            } catch (Exception e) {
                result = 101;
                Log.Fatal(e, "Application stopped.");
            }
            Log.CloseAndFlush();

            return result;
        }

        private static void HandleParseError(IEnumerable<Error> errs) {
            //handle errors
            Log.Error("There was a CLI error: " + errs.ToString());
        }
    }
}