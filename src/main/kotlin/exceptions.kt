class FileNotFound(file: String) :
        Exception("Couldn't find file $file") {}

class NotEnoughData() :
        Exception("Not enough data given") {}

class WrongDataFormat() :
        Exception("Wrong data format given")