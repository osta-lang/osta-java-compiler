package org.osta;

import org.osta.parse.ParseException;
import org.osta.parse.Parser;
import org.osta.parse.ast.AST;
import org.osta.parse.visitor.ILGenerator;
import org.osta.text.BufferCharSequence;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Main {

    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile("examples/simple.osta", "r");
        FileChannel channel = file.getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        BufferCharSequence input = new BufferCharSequence(buffer);

        try {
            AST ast = Parser.sequence(Parser.literal("hello"), Parser.item(), Parser.integer()).parse(input).ast();
            ILGenerator ilGenerator = new ILGenerator();
            ast.accept(ilGenerator);
            System.out.println(ilGenerator.generate());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
