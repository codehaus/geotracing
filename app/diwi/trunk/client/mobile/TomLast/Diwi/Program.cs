using System;
using System.IO;
using System.Collections.Generic;
using System.Windows.Forms;

namespace Diwi {
    static class Program {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>

        [MTAThread]
        static void Main() {
            Application.Run(new MainPage(null));
        }
    }
}