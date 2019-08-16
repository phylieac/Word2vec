# Word2vec
The program of word2vec for java, includes POS-CBOW and POS-Skip-gram
* POS-CBOW
  The continue bag of word language model with part-of-speech.
  
  ![image](https://github.com/phylieac/Word2vec/blob/master/POS-CBOW.png)
  
* POS-Skipgram
   The POS-Skipgram language model with part-of-speech
  ![image](https://github.com/phylieac/Word2vec/blob/master/POS-Skipgram.png)


# Usage

            Configuration conf=new Configuration();
            conf.setCBOW(0);
            conf.setThreads(2);
            conf.setMiniCount(0);
            Word2vec vectest=new Word2vec("text8","./cbow.bin",conf);
            vectest.train();

# License
  Apache Lisence 2.0
