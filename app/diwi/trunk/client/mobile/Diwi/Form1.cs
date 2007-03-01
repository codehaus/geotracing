using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace Diwi {
    public partial class Form1 : Form {
        
        public Form1() {
            InitializeComponent();
            Program.sKwxClient = KwxClient.instance;
            Program.sGpsReader = GpsReader.instance;
            new MainPage(this).ShowDialog();
        }
    }
}
